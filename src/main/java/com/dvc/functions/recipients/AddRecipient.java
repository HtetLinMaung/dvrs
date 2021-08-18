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
import com.dvc.models.SaveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AddRecipient {
    /**
     * This function listens at endpoint "/api/AddRecipient". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/AddRecipient 2. curl {your host}/api/AddRecipient?name=HTTP%20Query
     */
    @FunctionName("addrecipient")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            // TokenData tokenData = auth.getTokenData();
            RecipientsDto dto = new ObjectMapper().readValue(request.getBody().get(), RecipientsDto.class);
            // dto.setLoginuserid(tokenData.getUserid());
            // dto.setLoginusername(tokenData.getUsername());
            String partnersyskey = auth.getTokenData().getPartnersyskey();
            dto.setPartnersyskey(partnersyskey.isEmpty() ? "1" : partnersyskey);
            RecipientsDao dao = new RecipientsDao();

            if (dao.isAvailable("NRIC", dto.getNric()) || dao.isAvailable("Passport", dto.getPassport())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            long syskey = dao.addRecipient(dto);
            SaveResponse resData = new SaveResponse();
            resData.setSyskey(String.valueOf(syskey));
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
