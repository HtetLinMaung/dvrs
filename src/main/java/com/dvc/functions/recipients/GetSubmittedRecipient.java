package com.dvc.functions.recipients;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
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
public class GetSubmittedRecipient {
    /**
     * This function listens at endpoint "/api/GetSubmittedRecipient". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetSubmittedRecipient 2. curl {your
     * host}/api/GetSubmittedRecipient?name=HTTP%20Query
     */
    @FunctionName("getsubmittedrecipient")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            UpdateRecipientDto dto = new ObjectMapper().readValue(request.getBody().get(), UpdateRecipientDto.class);
            RecipientsDao dao = new RecipientsDao();
            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnRecipientV2(dto.getCid(), auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            List<Map<String, Object>> datalist = new RecipientsDao().getSubmittedRecipient(dto.getCid());
            SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
            if (datalist.size() == 0) {
                resData.setRetcode(ServerStatus.NO_DATA_ERROR);
                resData.setRetmessage(ServerMessage.NO_DATA_ERROR);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            }

            resData.setData(datalist.get(0));
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
