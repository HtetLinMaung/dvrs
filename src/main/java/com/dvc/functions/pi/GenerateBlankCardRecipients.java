package com.dvc.functions.pi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BaseDao;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.CenterDao;
import com.dvc.dao.PIDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BlankCardDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.QRYData;
import com.dvc.models.SingleResponse;
import com.dvc.models.TokenData;
import com.dvc.utils.CommonUtils;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.QRNewUtils;
import com.dvc.utils.Sender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GenerateBlankCardRecipients {
    /**
     * This function listens at endpoint "/api/GenerateBlankCardRecipients". Two
     * ways to invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GenerateBlankCardRecipients 2. curl {your
     * host}/api/GenerateBlankCardRecipients?name=HTTP%20Query
     */
    @FunctionName("generateblankcardrecipients")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
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
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            BlankCardDto dto = new ObjectMapper().readValue(request.getBody().get(), BlankCardDto.class);
            PIDao dao = new PIDao();
            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnPi(dto.getPisyskey(), auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            Map<String, Object> pi = new PIDao().getPi(Long.parseLong(dto.getPisyskey()));
            if (!pi.get("recordstatus").equals("30")) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            int currentcount = new BaseDao().getTotalCount("Recipients where pisyskey = ? and batchuploadsyskey <> 0",
                    Arrays.asList(dto.getPisyskey()));
            int totalcount = Integer.parseInt((String) pi.get("qty"));

            int remainingcount = totalcount - currentcount + Integer.parseInt((String) pi.get("voidcount"));

            if (remainingcount <= 0) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage("Already generated!");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            // int totalcount2 = new BaseDao().getTotalCount("ProformaInvoice where syskey =
            // ? and blankstatus = 1",
            // Arrays.asList(dto.getPisyskey()));
            // if (totalcount2 == 0) {
            List<String> serialRange = new CenterDao().addLastSerial((String) pi.get("centerid"),
                    Integer.parseInt((String) pi.get("qty")));

            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(dto.getBatchsyskey()));
            TokenData tokenData = auth.getTokenData();
            int i = 1;
            int j = 1;
            List<Map<String, Object>> recipients = new ArrayList<>();
            for (String serial : serialRange) {
                long rSyskey = KeyGenerator.generateSyskey();
                String now = Instant.now().toString();
                Map<String, Object> map = new HashMap<>();
                map.put("cid", serial);
                map.put("syskey", rSyskey);
                map.put("createddate", now);
                map.put("modifieddate", now);
                map.put("userid", tokenData.getDvrsuserid());
                map.put("username", tokenData.getDvrsusername());
                map.put("pisyskey", pi.get("syskey"));
                map.put("batchuploadsyskey", dto.getBatchsyskey());
                map.put("partnersyskey", pi.get("partnersyskey"));
                map.put("remark", "");
                map.put("rid", 0);
                map.put("recipientsname", "");
                map.put("fathername", "");
                map.put("gender", "");
                map.put("dob", null);
                map.put("age", 0);
                map.put("nric", "");
                map.put("passport", "");
                map.put("nationality", "");
                map.put("organization", "");
                map.put("address1", "");
                map.put("township", "");
                map.put("division", "");
                map.put("mobilephone", "");
                map.put("piref", pi.get("pirefnumber"));
                map.put("voidstatus", 1);
                map.put("centerid", pi.get("centerid"));
                map.put("batchrefcode", batch.get("batchrefcode") + "-" + String.valueOf(i++));
                map.put("vaccinationcenter", "");
                map.put("ward", "");
                map.put("street", "");
                map.put("prefixnrc", "");
                map.put("nrccode", "");
                map.put("nrctype", "");
                map.put("nrcno", "");
                map.put("occupation", "");
                QRYData data = new QRYData();
                data.setCid(serial);
                // if (pi.get("centerid").equals("YGN1")) {
                // int slot = (int) Math.ceil((double) j++ / 850);
                // int day = (int) Math.ceil((double) slot / 6);
                // int timeslot = slot - ((day - 1) * 6);
                // String firstdosetime = "";
                // switch (timeslot) {
                // case 1:
                // // 8:30
                // firstdosetime = "8:30 AM";
                // break;
                // case 2:
                // // 9:30
                // firstdosetime = "9:30 AM";
                // break;
                // case 3:
                // // 10:30
                // firstdosetime = "10:30 AM";
                // break;
                // case 4:
                // // 1:00
                // firstdosetime = "1:00 PM";
                // break;
                // case 5:
                // // 2:00
                // firstdosetime = "2:00 PM";
                // break;
                // case 6:
                // // 3:00
                // firstdosetime = "3:00 PM";
                // break;
                // }

                // map.put("firstdosedate",
                // DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.of(2021, 8,
                // 22).plusDays(day)));
                // map.put("firstdosetime", firstdosetime);
                // }
                // data.setDob("");
                // data.setName("");
                // data.setNric("");
                // data.setQraction("url");
                // data.setUrl("https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/"
                // + data.getCid());
                // data.setSyskey(String.valueOf(syskey));

                String qrtoken = QRNewUtils.generateQRToken(data);

                map.put("qrtoken", qrtoken);
                recipients.add(map);
            }

            new RecipientsDao().saveRecipientsFromPI(recipients);
            String cidrange = new RecipientsDao().getCidRange(Long.parseLong(dto.getBatchsyskey()));
            Map<String, Object> newbatch = new HashMap<>();
            newbatch.put("recipientsaved", 1);
            newbatch.put("syskey", dto.getBatchsyskey());
            newbatch.put("cidrange", cidrange);
            new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", newbatch);
            context.getLogger().info("Saved recipients finished for blank card batch " + batch.get("batchrefcode"));
            // Map<String, String> options = new HashMap<>();
            // options.put("Authorization", (String) messagedata.get("btoken"));
            // Map<String, Object> ret = RestClient.post(
            // "https://apx.registrationsystem.org/api/writeFiles?batchno=" +
            // String.valueOf(batchsyskey),
            // new HashMap<>(), options);
            // System.out.println(ret);
            pi = new HashMap<>();
            pi.put("syskey", dto.getPisyskey());
            // pi.put("blankstatus", 1);
            pi.put("balance", 0);
            pi.put("voidcount", 0);
            new PIDao().updatePI(pi);
            // CommonUtils.writeFiles(dto.getBatchsyskey());
            String isOverride = "1"; // 1 Override , 2 notOverride
            List<OutputBinding<String>> queueMsgList = Arrays.asList(messageOne, messageTwo, messageThree, messageFour,
                    messageFive, messageSix, messageSeven, messageEight, messageNine, messageTen);
            new CommonUtils().writeFiles(request, queueMsgList, dto.getBatchsyskey(),
                    new SingleResponse<Map<String, Object>>(), isOverride, false);
            // SimpleDateFormat format = new
            // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            // Date date = format.parse(Instant.now().toString());
            // Sender.sendEmail((String) batch.get("emailaddress"), String.format(
            // "<div>Dear User,</div>Your Record Card PDF is ready to
            // download.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch Number:
            // %s<br />Submission Date: %s<br />Thank you for using VRS 2021, Registration
            // System.",
            // batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
            // new SimpleDateFormat("dd/MM/yyyy").format(date)), "VRS 2021, Registration
            // System", "VRS");
            // }
            return request.createResponseBuilder(HttpStatus.OK).body(new BaseResponse()).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
