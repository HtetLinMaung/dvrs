package com.dvc.functions.auth;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.middlewares.ValidationMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.ModuleTokenDto;
import com.dvc.models.ModuleTokenResponse;
import com.dvc.utils.ApiUtil;
import com.dvc.utils.EasySql;
import com.dvc.utils.RestClient;
import com.dvc.utils.ServerUtil;
import com.dvc.utils.TokenUtil;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.factory.DbFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetModuleToken {
    /**
     * This function listens at endpoint "/api/GetModuleToken". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetModuleToken 2. curl {your
     * host}/api/GetModuleToken?name=HTTP%20Query
     */
    @FunctionName("getmoduletoken")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        try {
            ModuleTokenResponse res = new ModuleTokenResponse();
            MiddlewareData validation = ValidationMiddleware.checkValidation(request);
            if (!validation.isSuccess()) {
                return validation.getResponse();
            }

            ModuleTokenDto dto = new ObjectMapper().readValue(request.getBody().get(), ModuleTokenDto.class);

            if (ServerUtil.isBlank(dto.getAtoken())) {
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            if (!ApiUtil.isATokenValid(dto.getAtoken(), dto.getAppid(), dto.getUserid())) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            Map<String, Object> body = new HashMap<>();
            body.put("appid", "dvrs");
            body.put("domainid", "DDOI3XO9");
            Map<String, String> options = new HashMap<>();
            options.put("atoken", dto.getAtoken());

            Map<String, Object> ret = RestClient.post(System.getenv("IAM_URL") + "/userlevel", body, options);
            if (!"300".equals(ret.get("returncode"))) {
                res.setRetcode((String) ret.get("returncode"));
                res.setRetmessage((String) ret.get("message"));
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build();
            }

            Map<String, Object> payload = new EasySql(DbFactory.getConnection()).getOne(
                    Arrays.asList("partnersyskey", "dvrsuserid", "role", "partnerid", "partnertype", "partnername",
                            "contactperson", "dvrsusername", "pu.emailaddress"),
                    "PartnerUser as pu left join Partners as p on p.syskey = pu.partnersyskey where pu.recordstatus <> 4 and pu.emailaddress = ?",
                    Arrays.asList(dto.getUserid()));
            payload.put("userid", dto.getUserid());
            payload.put("appid", dto.getAppid());
            payload.put("role", ret.get("role"));
            // payload.put("role", "Partner");
            payload.put("userlevel", ret.get("userlevel"));
            String btoken = TokenUtil.getBToken(payload, true);
            res.setBtoken(btoken);
            res.setPartnername((String) payload.get("partnername"));
            res.setRole("Partner".equals((String) payload.get("role")) ? "" : (String) payload.get("role"));
            res.setPartnerid((String) payload.get("partnerid"));
            res.setRetcode(ServerStatus.SUCCESS);
            res.setRetmessage(ServerMessage.SUCCESS);

            return request.createResponseBuilder(HttpStatus.OK).body(res).build();

        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
