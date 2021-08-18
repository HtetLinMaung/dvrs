package com.dvc.functions.recipientsDownload;

import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with batch download recipients with zip HTTP Trigger
 */
public class CheckBatchDownload {
    /**
     * This function listens at endpoint "/api/checkBatch". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/downloadZip 2. curl "{your
     * host}/api/downloadZip?batchno=HTTP%20Query"
     */
    @FunctionName("checkBatch")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("checkBatch trigger processed a request.");

        final String query = request.getQueryParameters().get("batchno");
        final String batchNo = request.getBody().orElse(query);
        SingleResponse<Map<String, Object>> resData = new SingleResponse<>();

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            if (batchNo == null) {
                context.getLogger().warning("batch no null!");
                resData.setRetcode(ServerStatus.INVALID_REQUEST);
                resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            } else {
                long batch = 0l;
                try {
                    batch = Long.parseLong(batchNo);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                resData.setRetcode(ServerStatus.SUCCESS);
                resData.setRetmessage(
                        Integer.toString(new RecipientsDownloadDao().getBatchUploadStatusBySyskey(batch)));

                context.getLogger().info(String.format("checkBatch was finished with batch no : %s", batchNo));
                return request.createResponseBuilder(HttpStatus.OK).body(resData).build();
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
            resData.setRetcode(ServerStatus.SERVER_ERROR);
            resData.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(resData).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            resData.setRetcode(ServerStatus.SERVER_ERROR);
            resData.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(resData).build();
        }
    }

}
