package com.dvc.functions.qr;

import java.time.Instant;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.CenterDao;
import com.dvc.dao.LogDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.factory.DbFactory;
import com.dvc.models.BaseResponse;
import com.dvc.models.CheckQRDto;
import com.dvc.models.CheckQRResponse;
import com.dvc.utils.Cid;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.QRNewUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CheckQRToken {
    /**
     * This function listens at endpoint "/api/CheckQRToken". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/CheckQRToken 2. curl {your host}/api/CheckQRToken?name=HTTP%20Query
     */
    @FunctionName("checkqrtoken")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            BaseResponse res = new BaseResponse();
            if (!request.getBody().isPresent()) {
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build();
            }
            CheckQRDto dto = new ObjectMapper().readValue(request.getBody().get(), CheckQRDto.class);
            if (!dto.getAccesskey().equals(System.getenv("QRAccessKey")) || dto.getYtoken().isEmpty()) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build();
            }

            String cid = QRNewUtils.decryptY(dto.getYtoken());
            Map<String, Object> recipient = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList("dob", "nric",
                    "passport", "gender", "recipientsname", "syskey", "cid", "voidstatus", "dose", "t10"),
                    "Recipients where cid = ?", Arrays.asList(cid));
            int dose = Integer.parseInt((String) recipient.get("dose"));
            CheckQRResponse response = new CheckQRResponse();
            if ("url".equals(System.getenv("QR_ACTION"))) {
                response.setUrl("https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/" + cid);
            } else {
                String nric = (String) recipient.get("nric");
                String passport = (String) recipient.get("passport");
                String ic = "";
                if ((nric == null || nric.isEmpty()) && (passport == null || passport.isEmpty())) {
                    ic = "";
                } else if (nric == null || nric.isEmpty()) {
                    ic = passport;
                } else {
                    ic = nric;
                }
                LinkedHashMap<String, Object> rData = new LinkedHashMap<>();
                String cardid = cid.replaceAll("\\u0000", "");
                context.getLogger().info("cardid = " + cardid);
                context.getLogger().info("userid = " + dto.getUserid());
                if (!dto.getUserid().matches("^([A-Za-z0-9]{2,5})-([0-9]{2})@vrs$")
                        && !dto.getUserid().matches("admin-(.*)@vrs") && dto.getUserid().contains("@vrs")) {
                    if (recipient.get("voidstatus").equals("1")) {
                        rData.put("Valid Record Card" + (dose > 0 ? " - " + recipient.get("dose") + " dose(s)" : ""),
                                "true-mark");
                    } else {
                        rData.put("Void", "false-mark");
                    }
                    rData.put("CID " + (System.getenv("ICODE").equals("VRS") ? "(VRS)" : "(VRS UAT)"), cardid);
                    rData.put("Name", recipient.get("recipientsname"));
                    rData.put("NRC/PP", ic);
                    if (recipient.get("dob") != null) {
                        rData.put("Date of Birth", recipient.get("dob"));
                    } else {
                        rData.put("Date of Birth", "");
                    }

                    rData.put("Gender", recipient.get("gender"));
                    rData.put("Dose Details", recipient.get("t10"));
                } else if (dto.getUserid().matches("^([A-Za-z0-9]{2,5})-([0-9]{2})@vrs$")) {
                    int num = Integer.parseInt(dto.getUserid().split("-")[1].replaceAll("@", "")
                            .replaceAll("[a-zA-Z]", "").replaceAll("\\.", ""));

                    if (recipient.get("voidstatus").equals("1")) {
                        rData.put("Valid Record Card" + (dose > 0 ? " - " + recipient.get("dose") + " dose(s)" : ""),
                                "true-mark");
                    } else {
                        rData.put("Void", "false-mark");
                    }
                    if (Cid.getCenterFromCid(cardid).toLowerCase().equals(dto.getUserid().split("-")[0])
                            || (Cid.getCenterFromCid(cardid).equals("YGN")
                                    && dto.getUserid().split("-")[0].startsWith("ygn1"))) {
                        rData.put("Correct Center", "true-mark");
                    } else {
                        rData.put("Incorrect Center", "false-mark");
                    }
                    if (recipient.get("voidstatus").equals("1")
                            && (Cid.getCenterFromCid(cardid).toLowerCase().equals(dto.getUserid().split("-")[0])
                                    || (Cid.getCenterFromCid(cardid).equals("YGN")
                                            && dto.getUserid().split("-")[0].startsWith("ygn1")))
                            && num >= 50 && num <= 99) {
                        // rData.put("BTN-Update Dose " + String.valueOf(dose + 1),
                        // System.getenv("UPDATE_DOSE_URL")
                        // + "/updatedose?token=" + dto.getYtoken() + "&userid=" + dto.getUserid());
                        if (!new RecipientsDao().isDoseUpdated(cardid)) {
                            rData.put("BTN-Update Dose " + String.valueOf(dose + 1), System.getenv("UPDATE_DOSE_URL")
                                    + "/updatedose?token=" + dto.getYtoken() + "&userid=" + dto.getUserid());
                        }
                    }

                    rData.put("CID " + (System.getenv("ICODE").equals("VRS") ? "(VRS)" : "(VRS UAT)"), cardid);
                    rData.put("Name", recipient.get("recipientsname"));
                    if (num >= 1 && num <= 99) {
                        rData.put("NRC/PP", ic);
                        if (recipient.get("dob") != null) {
                            rData.put("Date of Birth", recipient.get("dob"));
                        } else {
                            rData.put("Date of Birth", "");
                        }

                        rData.put("Gender", recipient.get("gender"));
                    }

                    if (recipient.get("voidstatus").equals("1")
                            && (Cid.getCenterFromCid(cardid).toLowerCase().equals(dto.getUserid().split("-")[0])
                                    || (Cid.getCenterFromCid(cardid).equals("YGN")
                                            && dto.getUserid().split("-")[0].startsWith("ygn1")))
                            && num >= 50 && num <= 99) {
                        rData.put("TXT-1", "Lot No.");
                        rData.put("TXT-2", "Doctor/Nurse");
                        rData.put("TXT-3", "Remark");

                        context.getLogger().info(dto.getYtoken());
                    }
                    rData.put("Dose Details", recipient.get("t10"));
                } else if (!dto.getUserid().contains("@vrs")) {
                    if (recipient.get("voidstatus").equals("1")) {
                        rData.put("Valid Record Card" + (dose > 0 ? " - " + recipient.get("dose") + " dose(s)" : ""),
                                "true-mark");
                    } else {
                        rData.put("Void", "false-mark");
                    }
                    rData.put("CID " + (System.getenv("ICODE").equals("VRS") ? "(VRS)" : "(VRS UAT)"), cardid);
                    rData.put("Name", recipient.get("recipientsname"));
                } else if (dto.getUserid().matches("admin-(.*)@vrs")) {
                    if (recipient.get("voidstatus").equals("1")) {
                        rData.put("Valid Record Card" + (dose > 0 ? " - " + recipient.get("dose") + " dose(s)" : ""),
                                "true-mark");
                    } else {
                        rData.put("Void", "false-mark");
                    }
                    if (!new RecipientsDao().isDoseUpdated(cardid)) {
                        rData.put("BTN-Update Dose " + String.valueOf(dose + 1), System.getenv("UPDATE_DOSE_URL")
                                + "/updatedose?token=" + dto.getYtoken() + "&userid=" + dto.getUserid());
                    }

                    rData.put("CID " + (System.getenv("ICODE").equals("VRS") ? "(VRS)" : "(VRS UAT)"), cardid);
                    rData.put("Name", recipient.get("recipientsname"));
                    rData.put("NRC/PP", ic);
                    if (recipient.get("dob") != null) {
                        rData.put("Date of Birth", recipient.get("dob"));
                    } else {
                        rData.put("Date of Birth", "");
                    }

                    rData.put("Gender", recipient.get("gender"));
                    rData.put("TXT-1", "Lot No.");
                    rData.put("TXT-2", "Doctor/Nurse");
                    rData.put("TXT-3", "Remark");

                    rData.put("Dose Details", recipient.get("t10"));
                    context.getLogger().info(dto.getYtoken());

                }
                Map<String, Object> log = new HashMap<>();
                log.put("syskey", KeyGenerator.generateSyskey());
                log.put("cid", cardid);
                log.put("userid", dto.getUserid());
                log.put("verifyat", Instant.now().toString());
                new LogDao().addVerifyLog(log);
                response.setData(rData);
            }

            return request.createResponseBuilder(HttpStatus.OK).body(response).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
