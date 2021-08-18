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
import com.dvc.models.SaveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AddPI {
    /**
     * This function listens at endpoint "/api/AddPI". Two ways to invoke it using
     * "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/AddPI 2. curl
     * {your host}/api/AddPI?name=HTTP%20Query
     */
    @FunctionName("addpi")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            PIDto dto = new ObjectMapper().readValue(request.getBody().get(), PIDto.class);

            String partnersyskey = auth.getTokenData().getPartnersyskey();
            dto.setPartnersyskey(
                    !auth.getTokenData().getRole().equals("Partner") ? dto.getPartnersyskey() : partnersyskey);
            PIDao dao = new PIDao();

            Map<String, Object> data = dao.addPI(dto);
            SaveResponse resData = new SaveResponse();
            resData.setRetmessage(data.get("pirefnumber") + " Submitted Successfully");
            resData.setSyskey(String.valueOf(data.get("syskey")));

            return request.createResponseBuilder(HttpStatus.CREATED).body(resData).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
