package com.dvc.functions.pi;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PIDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PIDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UpdatePI {
    /**
     * This function listens at endpoint "/api/UpdatePI". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/UpdatePI
     * 2. curl {your host}/api/UpdatePI?name=HTTP%20Query
     */
    @FunctionName("updatepi")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            PIDto dto = new ObjectMapper().readValue(request.getBody().get(), PIDto.class);
            // TokenData tokenData = auth.getTokenData();
            // dto.setLoginuserid(tokenData.getUserid());
            // dto.setLoginusername(tokenData.getUsername());
            String partnersyskey = auth.getTokenData().getPartnersyskey();
            PIDao dao = new PIDao();
            if (auth.getTokenData().getRole().equals("Partner") && !dao.isOwnPi(dto.getSyskey(), partnersyskey)) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            dto.setPartnersyskey(
                    !auth.getTokenData().getRole().equals("Partner") ? dto.getPartnersyskey() : partnersyskey);
            int result = new PIDao().updatePI(dto);
            BaseResponse resData = new BaseResponse();
            if (result == 0) {
                resData.setRetcode(ServerStatus.INVALID_REQUEST);
                resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            }

            return request.createResponseBuilder(HttpStatus.OK).body(resData).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
