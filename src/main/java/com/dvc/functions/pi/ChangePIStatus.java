package com.dvc.functions.pi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.CenterDao;
import com.dvc.dao.PIDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PIDto;
import com.dvc.models.QRYData;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.QRUtils;
import com.dvc.utils.Sender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ChangePIStatus {
    /**
     * This function listens at endpoint "/api/ChangePIStatus". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ChangePIStatus 2. curl {your
     * host}/api/ChangePIStatus?name=HTTP%20Query
     */
    @FunctionName("changepistatus")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            BaseResponse res = new BaseResponse();
            if (!Arrays.asList("Finance", "Admin").contains(auth.getTokenData().getRole())) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            PIDto dto = new ObjectMapper().readValue(request.getBody().get(), PIDto.class);
            PIDao dao = new PIDao();

            Map<String, Object> args = new HashMap<>();
            args.put("syskey", dto.getSyskey());
            Map<String, Object> pi = dao.getPi(Long.parseLong(dto.getSyskey()));
            final String now = Instant.now().toString();
            switch (dto.getStatustype()) {
                case 1:

                    if (pi.get("recordstatus").equals("30")) {
                        res.setRetcode(ServerStatus.UNAUTHORIZED);
                        res.setRetmessage("Already Approved");
                        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                    }
                    if (dto.getRecordstatus() != 30) {
                        res.setRetcode(ServerStatus.UNAUTHORIZED);
                        res.setRetmessage(ServerMessage.UNAUTHORIZED);
                        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
                    }

                    args.put("recordstatus", dto.getRecordstatus());
                    args.put("approveddate", now);

                    // List<String> serialRange = new CenterDao().addLastSerial((String)
                    // pi.get("centerid"),
                    // Integer.parseInt((String) pi.get("qty")));

                    // List<Map<String, Object>> recipients = serialRange.stream().map(serial -> {
                    // long syskey = KeyGenerator.generateSyskey();
                    // String now = Instant.now().toString();
                    // Map<String, Object> map = new HashMap<>();
                    // map.put("cid", serial);
                    // map.put("syskey", syskey);
                    // // map.put("rid", KeyGenerator.generateID());
                    // map.put("createddate", now);
                    // map.put("modifieddate", now);
                    // map.put("userid", auth.getTokenData().getUserid());
                    // map.put("username", auth.getTokenData().getDvrsusername());
                    // map.put("pisyskey", dto.getSyskey());
                    // map.put("batchuploadsyskey", 0);
                    // map.put("partnersyskey", pi.get("partnersyskey"));
                    // map.put("remark", "");
                    // map.put("rid", 0);
                    // map.put("recipientsname", "");
                    // map.put("fathername", "");
                    // map.put("gender", "");
                    // map.put("dob", null);
                    // map.put("age", 0);
                    // map.put("nric", "");
                    // map.put("passport", "");
                    // map.put("nationality", "");
                    // map.put("organization", "");
                    // map.put("address1", "");
                    // map.put("township", "");
                    // map.put("division", "");
                    // map.put("mobilephone", "");
                    // map.put("piref", pi.get("pirefnumber"));

                    // QRYData data = new QRYData();
                    // data.setCid(serial);
                    // data.setDob("");
                    // data.setName("");
                    // data.setNric("");
                    // data.setQraction("json");
                    // data.setSyskey(String.valueOf(syskey));
                    // String qrtoken = "";
                    // try {
                    // qrtoken = QRUtils.generateQRToken(data);
                    // } catch (IOException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    // map.put("qrtoken", qrtoken);
                    // return map;
                    // }).collect(Collectors.toList());
                    // new RecipientsDao().saveRecipientsFromPI(recipients);
                    break;
                case 2:
                    args.put("paymentstatus", dto.getPaymentstatus());
                    // args.put("paymentdate", dto.getPaymentdate());
                    break;
            }

            int result = new PIDao().updatePI(args);
            if (result != 0 && dto.getStatustype() == 1 && dto.getRecordstatus() == 30) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = format.parse(now);

                Sender.sendEmail((String) pi.get("emailaddress"), String.format(
                        "<div>Dear User,</div>Your Batch/Payment has been approved.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch/PI Number: %s<br />Submission Date: %s<br />Bank: %s<br />Reference: %s<br /><br />Thank you for using VRS 2021, Registration System.",
                        pi.get("partnerid"), pi.get("partnername"), pi.get("pirefnumber"),
                        new SimpleDateFormat("dd/MM/yyyy").format(date), pi.get("bankname"), pi.get("paymentref")),
                        "VRS 2021, Registration System", "VRS");
            }
            res.setRetmessage(pi.get("pirefnumber") + " Approved Successfully");
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
