package com.dvc.functions.dose;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.models.BaseResponse;
import com.dvc.utils.QRNewUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UpdateDose {
    /**
     * This function listens at endpoint "/api/UpdateDose". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UpdateDose 2. curl {your host}/api/UpdateDose?name=HTTP%20Query
     */
    @FunctionName("updatedose")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            String txt1 = request.getQueryParameters().get("txt1");
            String txt2 = request.getQueryParameters().get("txt2");
            String txt3 = request.getQueryParameters().get("txt3");
            String token = request.getQueryParameters().get("token");
            String userid = request.getQueryParameters().get("userid");
            if (token.split(" ").length > 1) {
                token = String.join("+", token.split(" "));
            }
            context.getLogger().info(token);
            String cid = QRNewUtils.decryptY(token).replaceAll("\\u0000", "");

            RecipientsDao dao = new RecipientsDao();
            Map<String, Object> r = dao.getRecipientByKey("cid", cid);
            // Map<String, Object> info = new HashMap<>();
            // info.put("batch", txt1);
            // info.put("doctor", txt2);
            // info.put("dose", txt3);
            // info.put("cid", cid);
            // info.put("nrc", r.get("nric"));
            // info.put("dob", r.get("dob"));
            // info.put("name", r.get("recipientsname"));
            // info.put("userid", userid);
            // dao.addDoseInfo(info);
            Map<String, Object> args = new HashMap<>();
            // 23/8/2021, Dr ABC, lot 123456,1;
            String t10 = r.get("t10") == null ? "" : (String) r.get("t10");
            LocalDateTime datetime = LocalDateTime.now();
            args.put("syskey", r.get("syskey"));
            args.put("dose", Integer.parseInt((String) r.get("dose")) + 1);
            args.put("t10", t10 + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(datetime) + ", " + txt2 + ", lot "
                    + txt1 + ", " + "1" + ";");
            dao.updateRecipient(args);

            context.getLogger().info(new ObjectMapper().writeValueAsString(request.getQueryParameters()));
            BaseResponse res = new BaseResponse();
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
