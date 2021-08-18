package com.dvc.functions.recipients;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.RecipientsDto;
import com.dvc.models.SingleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetRecipient {
    /**
     * This function listens at endpoint "/api/GetRecipient". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetRecipient 2. curl {your host}/api/GetRecipient?name=HTTP%20Query
     */
    @FunctionName("getrecipient")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            RecipientsDto dto = new ObjectMapper().readValue(request.getBody().get(), RecipientsDto.class);
            RecipientsDao dao = new RecipientsDao();
            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnRecipient(dto.getSyskey(), auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            Map<String, Object> data = new RecipientsDao().getRecipient(Long.parseLong(dto.getSyskey()));
            SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
            if (((String) data.get("syskey")).isEmpty()) {
                resData.setRetcode(ServerStatus.INVALID_REQUEST);
                resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            }
            resData.setData(data);
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
