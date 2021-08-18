package com.dvc.functions.batchupload;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.PIDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.CommonUtils;
import com.dvc.utils.Sender;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class SaveApprovedRecipients {
    /**
     * This function listens at endpoint "/api/SaveApprovedRecipients". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/SaveApprovedRecipients 2. curl {your
     * host}/api/SaveApprovedRecipients?name=HTTP%20Query
     */
    @FunctionName("saveapprovedrecipients")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
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
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            final String syskey = request.getQueryParameters().get("id");
            BatchUploadDao dao = new BatchUploadDao();

            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnBatch(syskey, auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            Map<String, Object> batch = dao.getBatch(Long.parseLong(syskey));
            int status = Integer.parseInt((String) batch.get("recordstatus"));

            // if not verified
            BaseResponse res = new BaseResponse();
            if (status != 30) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            List<Map<String, Object>> validdatalist = dao.getBatchDetailsByHeader(Long.parseLong(syskey), 1);
            Map<String, Object> pi = new PIDao().getPi(Long.parseLong((String) batch.get("pisyskey")));
            if (pi.get("recordstatus").equals("30")) {
                BatchDto dtoData = new BatchDto();
                dtoData.setDatalist(validdatalist);
                dtoData.setBatchsyskey(syskey);
                dtoData.setPisyskey((String) batch.get("pisyskey"));
                dtoData.setCenterid((String) batch.get("centerid"));
                dtoData.setPartnersyskey((String) batch.get("partnersyskey"));
                // String btoken = request.getHeaders().get("authorization");

                dao.saveRecipents(dtoData, context);
                context.getLogger().info("Finished Saved recipients for batch " + batch.get("batchrefcode"));
                String isOverride = "1"; // 1 Override , 2 notOverride
                List<OutputBinding<String>> queueMsgList = Arrays.asList(messageOne, messageTwo, messageThree,
                        messageFour, messageFive, messageSix, messageSeven, messageEight, messageNine, messageTen);
                new CommonUtils().writeFiles(request, queueMsgList, (String) batch.get("syskey"),
                        new SingleResponse<Map<String, Object>>(), isOverride, false);
                // CommonUtils.writeFiles((String) batch.get("syskey"));

                Map<String, Object> newpi = new HashMap<>();
                int remaincount = Integer.parseInt((String) pi.get("balance")) - validdatalist.size();
                newpi.put("syskey", pi.get("syskey"));
                newpi.put("balance", remaincount);
                new PIDao().updatePI(newpi);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = format.parse(Instant.now().toString());
                // Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                // "<div>Dear User,</div>Your Record Card PDF is ready to
                // download.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch Number:
                // %s<br />Submission Date: %s<br />Thank you for using VRS 2021, Registration
                // System.",
                // batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                // new SimpleDateFormat("dd/MM/yyyy").format(date)), "VRS 2021, Registration
                // System", "VRS");
            }
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
