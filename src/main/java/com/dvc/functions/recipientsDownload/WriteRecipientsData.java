package com.dvc.functions.recipientsDownload;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.AESAlgorithm;
import com.dvc.utils.CommonUtils;
import com.dvc.utils.ServerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;

/**
 * Azure Functions with batch download recipients with zip HTTP Trigger
 */
public class WriteRecipientsData {
    /**
     * This function listens at endpoint "/api/writeFiles". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/downloadZip 2. curl "{your
     * host}/api/downloadZip?batchno=HTTP%2PDFUtil.INDEX_ZEROQuery"
     */
    @FunctionName("writeFiles")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @QueueOutput(name = "messageOne", queueName = "pdf-queue-one", connection = "AzureStorageConnectionString") OutputBinding<String> messageOne,
            @QueueOutput(name = "messageTwo", queueName = "pdf-queue-two", connection = "AzureStorageConnectionString") OutputBinding<String> messageTwo,
            @QueueOutput(name = "messageThree", queueName = "pdf-queue-three", connection = "AzureStorageConnectionString") OutputBinding<String> messageThree,
            @QueueOutput(name = "messageFour", queueName = "pdf-queue-four", connection = "AzureStorageConnectionString") OutputBinding<String> messageFour,
            @QueueOutput(name = "messageFive", queueName = "pdf-queue-five", connection = "AzureStorageConnectionString") OutputBinding<String> messageFive,
            @QueueOutput(name = "messageSix", queueName = "pdf-queue-six", connection = "AzureStorageConnectionString") OutputBinding<String> messageSix,
            @QueueOutput(name = "messageSeven", queueName = "pdf-queue-seven", connection = "AzureStorageConnectionString") OutputBinding<String> messageSeven,
            @QueueOutput(name = "messageEight", queueName = "pdf-queue-eight", connection = "AzureStorageConnectionString") OutputBinding<String> messageEight,
            @QueueOutput(name = "messageNine", queueName = "pdf-queue-nine", connection = "AzureStorageConnectionString") OutputBinding<String> messageNine,
            @QueueOutput(name = "messageTen", queueName = "pdf-queue-ten", connection = "AzureStorageConnectionString") OutputBinding<String> messageTen,
            final ExecutionContext context) {
        context.getLogger().info("writeFiles trigger processed a request.");

        final String query = request.getQueryParameters().get("batchno");
        String batchNo = request.getBody().orElse(query);
        /*
         * final String overrideFlag = request.getQueryParameters().get("code"); final
         * String overrideCode = request.getBody().orElse(overrideFlag);
         */
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
                String btoken = request.getHeaders().get("authorization");
                if (btoken != null) {
                    if (btoken.contains("Bearer")) {
                        btoken = btoken.replace("Bearer ", "");
                    }
                }

                String encodedURI = batchNo.replace(" ", "+");
                batchNo = AESAlgorithm.decryptString(encodedURI);
                if (batchNo == null) {
                    batchNo = "0";
                }
                new RecipientsDownloadDao().updateStatus(Long.parseLong(batchNo));
                String isOverride = "1"; // 1 Override , 2 notOverride
                List<OutputBinding<String>> queueMsgList = Arrays.asList(messageOne, messageTwo, messageThree,
                        messageFour, messageFive, messageSix, messageSeven, messageEight, messageNine, messageTen);
                return new CommonUtils().writeFiles(request, queueMsgList, batchNo, resData, isOverride, true);

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
