package com.dvc.functions.pi;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BaseDao;
import com.dvc.dao.BatchUploadDao;

import com.dvc.dao.PIDao;

import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.MiddlewareData;

import com.dvc.models.TokenData;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GenerateBlankCard {
    /**
     * This function listens at endpoint "/api/GenerateBlankCard". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GenerateBlankCard 2. curl {your
     * host}/api/GenerateBlankCard?name=HTTP%20Query
     */
    @FunctionName("generateblankcard")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            final String syskey = request.getQueryParameters().get("pisyskey");
            PIDao dao = new PIDao();
            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnPi(syskey, auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            Map<String, Object> pi = new PIDao().getPi(Long.parseLong(syskey));
            if (!pi.get("recordstatus").equals("30")) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            int currentcount = new BaseDao().getTotalCount("Recipients where pisyskey = ? and batchuploadsyskey <> 0",
                    Arrays.asList(syskey));
            int totalcount = Integer.parseInt((String) pi.get("qty"));

            int remainingcount = totalcount - currentcount + Integer.parseInt((String) pi.get("voidcount"));

            if (remainingcount <= 0) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage("Already generated!");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            TokenData tokenData = auth.getTokenData();
            String partnersyskey = (String) new EasySql(DbFactory.getConnection())
                    .getOne(Arrays.asList("syskey"), "Partners where partnerid = ?", Arrays.asList(pi.get("partnerid")))
                    .get("syskey");
            BatchDto dto = new BatchDto();
            dto.setPartnerid((String) pi.get("partnerid"));
            dto.setPartnersyskey(partnersyskey);
            dto.setUserid(tokenData.getDvrsuserid());
            dto.setUsername(tokenData.getDvrsusername());
            long batchsyskey = new BatchUploadDao().saveBatchHeader(dto, pi);
            // Map<String, Object> batch = new BatchUploadDao().getBatch(batchsyskey);
            // String btoken = request.getHeaders().get("authorization");

            // List<String> serialRange = new CenterDao().addLastSerial((String)
            // pi.get("centerid"),
            // Integer.parseInt((String) pi.get("qty")));

            // int i = 1;
            // List<Map<String, Object>> recipients = new ArrayList<>();
            // for (String serial : serialRange) {
            // long rSyskey = KeyGenerator.generateSyskey();
            // String now = Instant.now().toString();
            // Map<String, Object> map = new HashMap<>();
            // map.put("cid", serial);
            // map.put("syskey", rSyskey);
            // map.put("createddate", now);
            // map.put("modifieddate", now);
            // map.put("userid", auth.getTokenData().getUserid());
            // map.put("username", auth.getTokenData().getDvrsusername());
            // map.put("pisyskey", pi.get("syskey"));
            // map.put("batchuploadsyskey", batchsyskey);
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
            // map.put("voidstatus", 1);
            // map.put("batchrefcode", batch.get("batchrefcode") + "-" +
            // String.valueOf(i++));
            // QRYData data = new QRYData();
            // data.setCid(serial);
            // data.setDob("");
            // data.setName("");
            // data.setNric("");
            // data.setQraction("url");
            // data.setUrl("https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/"
            // + data.getCid());
            // data.setSyskey(String.valueOf(syskey));
            // String qrtoken = "";

            // qrtoken = QRUtils.generateQRToken(data);

            // map.put("qrtoken", qrtoken);
            // recipients.add(map);
            // }

            // new RecipientsDao().saveRecipientsFromPI(recipients);

            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("batchsyskey", String.valueOf(batchsyskey));
            res.put("pisyskey", syskey);
            res.put("qty", pi.get("qty"));
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
