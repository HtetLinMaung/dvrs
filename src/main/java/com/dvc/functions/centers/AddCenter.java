package com.dvc.functions.centers;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.CenterDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.CenterDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SaveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AddCenter {
    /**
     * This function listens at endpoint "/api/AddCenter". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/AddCenter 2. curl {your host}/api/AddCenter?name=HTTP%20Query
     */
    @FunctionName("addcenter")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            if (auth.getTokenData().getRole().equals("Partner")) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            CenterDto dto = new ObjectMapper().readValue(request.getBody().get(), CenterDto.class);
            if (!dto.getCenterid().matches("^([a-zA-Z]{1,3}[0-9]{1,2})$")) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage("Invalid Center ID");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            long syskey = new CenterDao().addCenter(dto);
            SaveResponse res = new SaveResponse();
            res.setSyskey(String.valueOf(syskey));
            return request.createResponseBuilder(HttpStatus.CREATED).body(res).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
