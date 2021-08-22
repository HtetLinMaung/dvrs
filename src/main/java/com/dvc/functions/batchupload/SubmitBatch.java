package com.dvc.functions.batchupload;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BaseDao;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.CenterDao;
import com.dvc.dao.PIDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.Sender;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class SubmitBatch {
    /**
     * This function listens at endpoint "/api/SubmitBatch". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/SubmitBatch 2. curl {your host}/api/SubmitBatch?name=HTTP%20Query
     */
    @FunctionName("submitbatch")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
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
            if (status != 10) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            long pisyskey = Long.parseLong((String) batch.get("pisyskey"));
            // int currentcount = new BaseDao().getTotalCount("Recipients where pisyskey = ?
            // and batchuploadsyskey <> 0",
            // Arrays.asList(pisyskey));

            Map<String, Object> pi = new PIDao().getPi(pisyskey);
            // int totalcount = Integer.parseInt((String) pi.get("qty"));

            // int remainingcount = totalcount - currentcount;
            // if (remainingcount < 0) {
            // remainingcount = 0;
            // }
            if (!pi.get("recordstatus").equals("30")) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage("Payment must be approved");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            List<Map<String, Object>> validdatalist = dao.getBatchDetailsByHeader(Long.parseLong(syskey), 1);

            if (validdatalist.size() == 0) {
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage("No valid records");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            if (validdatalist.size() > Integer.parseInt((String) pi.get("balance"))
                    + Integer.parseInt((String) pi.get("voidcount"))) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage("Insufficient balance");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            Map<String, Object> args = new HashMap<>();
            final String now = Instant.now().toString();
            args.put("syskey", syskey);
            args.put("modifieddate", now);
            if (pi.get("recordstatus").equals("30")) {
                // BatchDto dtoData = new BatchDto();
                // dtoData.setDatalist(validdatalist);
                // dtoData.setBatchsyskey(syskey);
                // dtoData.setPisyskey((String) batch.get("pisyskey"));
                // dtoData.setCenterid((String) batch.get("centerid"));
                // dtoData.setPartnersyskey((String) batch.get("partnersyskey"));
                // String btoken = request.getHeaders().get("authorization");
                args.put("approveddate", now);
                args.put("submitteddate", now);
                args.put("recordstatus", 30);
                dao.updateBatch(args);

                // dao.saveRecipents(dtoData, btoken);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = format.parse(now);
                Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                        "<div>Dear User,</div>Your Batch/Payment has been approved.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch/PI Number: %s<br />Submission Date: %s<br />Bank: %s<br />Reference: %s<br /><br />Thank you for using VRS 2021, Registration System.",
                        batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                        new SimpleDateFormat("dd/MM/yyyy").format(date), pi.get("bankname"), pi.get("paymentref")),
                        "VRS 2021, Registration System", "VRS");
                res.setRetmessage(batch.get("batchrefcode") + " Approved Successfully");
            } else {
                args.put("submitteddate", now);
                args.put("recordstatus", 20);
                dao.updateBatch(args);
                res.setRetmessage(batch.get("batchrefcode") + " Submitted Successfully");
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
