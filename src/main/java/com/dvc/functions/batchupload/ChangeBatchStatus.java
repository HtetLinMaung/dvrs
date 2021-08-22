package com.dvc.functions.batchupload;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.PIDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.BatchStatus;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.Sender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ChangeBatchStatus {
    /**
     * This function listens at endpoint "/api/ChangeBatchStatus". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ChangeBatchStatus 2. curl {your
     * host}/api/ChangeBatchStatus?name=HTTP%20Query
     */
    @FunctionName("changebatchstatus")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            BatchDto dto = new ObjectMapper().readValue(request.getBody().get(), BatchDto.class);
            BatchUploadDao dao = new BatchUploadDao();
            BaseResponse res = new BaseResponse();

            final String now = Instant.now().toString();
            for (BatchStatus bStatus : dto.getStatuslist()) {
                if (auth.getTokenData().getRole().equals("Partner")
                        && !dao.isOwnBatch(bStatus.getSyskey(), auth.getTokenData().getPartnersyskey())) {
                    res.setRetcode(ServerStatus.UNAUTHORIZED);
                    res.setRetmessage(ServerMessage.UNAUTHORIZED);
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                }
                Map<String, Object> args = new HashMap<>();
                args.put("syskey", bStatus.getSyskey());
                args.put("modifieddate", now);
                String key = "voidstatus";
                switch (bStatus.getStatustype()) {
                    case 1:
                        Map<String, Object> batch = dao.getBatch(Long.parseLong(bStatus.getSyskey()));
                        if (!Arrays.asList("Finance", "Admin").contains(auth.getTokenData().getRole())
                                || bStatus.getStatus() != 30) {
                            res.setRetcode(ServerStatus.UNAUTHORIZED);
                            res.setRetmessage(ServerMessage.UNAUTHORIZED);
                            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                        }

                        if (!batch.get("recordstatus").equals("20")) {
                            res.setRetcode(ServerStatus.UNAUTHORIZED);
                            res.setRetmessage(ServerMessage.UNAUTHORIZED);
                            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                        }
                        if (batch.get("recordstatus").equals("30")) {
                            res.setRetcode(ServerStatus.UNAUTHORIZED);
                            res.setRetmessage("Already Approved");
                            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                        }

                        Map<String, Object> pi = new PIDao().getPi(Long.parseLong((String) batch.get("pisyskey")));
                        if (!pi.get("recordstatus").equals(30)) {
                            res.setRetcode(ServerStatus.UNAUTHORIZED);
                            res.setRetmessage(ServerMessage.UNAUTHORIZED);
                            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                        }
                        args.put("approveddate", now);

                        List<Map<String, Object>> validdatalist = dao
                                .getBatchDetailsByHeader(Long.parseLong(bStatus.getSyskey()), 1);

                        if (validdatalist.size() == 0) {
                            res.setRetcode(ServerStatus.INVALID_REQUEST);
                            res.setRetmessage("There are no valid records!");
                            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                        }

                        // BatchDto dtoData = new BatchDto();
                        // dtoData.setDatalist(validdatalist);
                        // dtoData.setBatchsyskey(bStatus.getSyskey());
                        // dtoData.setPisyskey((String) batch.get("pisyskey"));
                        // dtoData.setCenterid((String) batch.get("centerid"));
                        // dtoData.setPartnersyskey((String) batch.get("partnersyskey"));
                        // String btoken = request.getHeaders().get("authorization");
                        key = "recordstatus";
                        args.put(key, bStatus.getStatus());
                        new BatchUploadDao().updateBatch(args);
                        // dao.saveRecipents(dtoData, btoken);
                        // message.setValue(bStatus.getSyskey());
                        if (bStatus.getStatustype() == 1 && bStatus.getStatus() == 30) {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            Date date = format.parse(now);
                            Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                                    "<div>Dear User,</div>Your Batch/Payment has been approved.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch/PI Number: %s<br />Submission Date: %s<br />Bank: %s<br />Reference: %s<br /><br />Thank you for using VRS 2021, Registration System.",
                                    batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                                    new SimpleDateFormat("dd/MM/yyyy").format(date), pi.get("bankname"),
                                    pi.get("paymentref")), "VRS 2021, Registration System", "VRS");
                        }

                        break;
                    case 2:
                        key = "paymentstatus";
                        args.put(key, bStatus.getStatus());
                        new BatchUploadDao().updateBatch(args);
                        break;
                    case 3:
                        args.put("voidstatus", bStatus.getStatus());
                        new BatchUploadDao().updateBatch(args);
                        new RecipientsDao().voidRecipientsByBatch(bStatus.getSyskey(), bStatus.getStatus());
                        break;
                }

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
