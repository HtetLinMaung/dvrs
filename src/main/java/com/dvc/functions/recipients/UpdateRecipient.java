package com.dvc.functions.recipients;

import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.RecipientsDto;
import com.dvc.models.TokenData;
import com.dvc.models.UpdateRecipientDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UpdateRecipient {
    /**
     * This function listens at endpoint "/api/UpdateRecipient". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UpdateRecipient 2. curl {your
     * host}/api/UpdateRecipient?name=HTTP%20Query
     */
    @FunctionName("updaterecipient")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            UpdateRecipientDto dto = new ObjectMapper().readValue(request.getBody().get(), UpdateRecipientDto.class);

            TokenData tokenData = auth.getTokenData();
            dto.setUserid(tokenData.getDvrsuserid());
            dto.setUsername(tokenData.getDvrsusername());
            String partnersyskey = auth.getTokenData().getPartnersyskey();
            
            int result = new RecipientsDao().updateRecipient(dto);
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
