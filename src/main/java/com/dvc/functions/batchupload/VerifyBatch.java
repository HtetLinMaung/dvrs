package com.dvc.functions.batchupload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.microsoft.azure.functions.annotation.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dvc.constants.CommonConstants;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.dvc.utils.ExcelUtil;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.Sender;
import com.dvc.utils.ValidateBatchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class VerifyBatch {
    /**
     * This function listens at endpoint "/api/VerifyBatch". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/VerifyBatch 2. curl {your host}/api/VerifyBatch?name=HTTP%20Query
     */
    @FunctionName("verifybatch")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            Instant start = Instant.now();
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
            context.getLogger().info("fetching batch by syskey...");
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(syskey));
            context.getLogger().info(batch.get("batchrefcode") + " fetching batch by syskey completed");

            int status = Integer.parseInt((String) batch.get("recordstatus"));
            if (status >= 20) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            final String now = Instant.now().toString();

            String url = batch.get("fileurl") + "?" + AzureBlobUtils.getSasToken();
            Map<String, String> firstRow = ExcelUtil.getExcelFirstRow(new URL(url).openStream(),
                    CommonConstants.HEADERS);

            List<String> headers = ExcelUtil.getHeaders(url);

            List<Map<String, Object>> detaillist = new ArrayList<>();
            for (Map<String, Object> m : ExcelUtil.excelToDataList(new URL(url).openStream(), headers)) {

                String str = (String) m.get("serialno");
                if (str.isEmpty()) {
                    break;
                }
                try {
                    Integer.parseInt(str.split("\\.")[0]);
                } catch (Exception e) {
                    break;
                }
                try {
                    String serialno = "0";
                    if (str.split("\\.").length > 0) {
                        serialno = str.split("\\.")[0];
                    }
                    m.put("mobilephone", ValidateBatchUtils.normalizePhone((String) m.get("mobilephone")));
                    m.put("serialno", serialno);
                    m.put("syskey", KeyGenerator.generateSyskey());
                    m.put("createddate", now);
                    m.put("modifieddate", now);
                    m.put("recordstatus", 0);
                    m.put("vaccinationstatus", 0);
                    m.put("piref", ""); // recheck
                    m.put("batchuploadsyskey", syskey);
                    m.put("partnersyskey", batch.get("partnersyskey"));
                    m.put("nric", m.get("nric") == null ? "" : ValidateBatchUtils.normalizeNrc((String) m.get("nric")));
                    m.put("dob", ValidateBatchUtils.normalizeDob((String) m.get("dob")));
                    m.put("batchrefcode", String.format("%s-%s", batch.get("batchrefcode"), serialno));
                    context.getLogger().info(batch.get("batchrefcode") + " Reading serialno " + serialno);
                    detaillist.add(m);
                } catch (Exception rowe) {
                    context.getLogger().severe(rowe.getMessage());
                }
            }

            new EasySql(DbFactory.getConnection()).deleteOne("BatchDetails", "batchuploadsyskey", syskey);
            new EasySql(DbFactory.getConnection()).insertMany("BatchDetails", detaillist);
            Map<String, Object> newbatch = new HashMap<>();
            newbatch.put("syskey", syskey);
            newbatch.put("uploadedrecords", detaillist.size());
            new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", newbatch);
            context.getLogger().info(batch.get("batchrefcode")
                    + " Writing batch detail to database with total records: " + String.valueOf(detaillist.size()));

            Map<String, String> headerDesc = ExcelUtil.getExcelFirstRow(new URL(url).openStream(), headers);
            ValidationResult result = new ValidationResult();
            List<ResultInfo> infolist = new ArrayList<>();
            Workbook workbook = new XSSFWorkbook(new URL(url).openStream());
            Sheet sheet = workbook.getSheetAt(0);
            String oldNric = "";
            String oldPassport = "";
            String oldName = "";
            List<Row> excludeList = new ArrayList<>();
            List<Map<String, Object>> datalist = new ArrayList<>();

            int i = 0;
            int j = 1;
            for (Row row : sheet) {
                if (j == 1) {
                    j++;
                    continue;
                }
                if (detaillist.size() == i)
                    break;

                Instant detailstart = Instant.now();
                List<String> descriptionlist = new ArrayList<>();
                boolean isValid = true;
                List<Map<String, Object>> keys = new ArrayList<>();
                ResultInfo info = new ResultInfo();

                Map<String, String> data = ExcelUtil.getMapFromRow(row, headers, workbook);

                if (data.get("serialno").isEmpty()) {
                    break;
                }
                try {
                    Integer.parseInt(data.get("serialno").split("\\.")[0]);
                } catch (Exception e) {
                    break;
                }
                for (String key : Arrays.asList("serialno", "recipientsname")) {
                    // "nameicpp", "division", "township", "address1"
                    if (((String) data.get(key)).isEmpty()) {
                        isValid = false;
                        descriptionlist.add(headerDesc.get(key).trim() + " is blank");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", key);
                        keyData.put("description", headerDesc.get(key).trim() + " is blank");
                        keys.add(keyData);
                    }
                }

                if (!ValidateBatchUtils.isGenderValid(((String) data.get("gender")))) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("gender") + " must be M or F");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "gender");
                    keyData.put("description", headerDesc.get("gender") + " must be M or F");
                    keys.add(keyData);
                }

                if (!ValidateBatchUtils.isDobValid((String) data.get("dob"))) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("dob") + " must be DD/MM/YYYY or not under 18 years");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "dob");
                    keyData.put("description", headerDesc.get("dob") + " must be DD/MM/YYYY or not under 18 years");
                    keys.add(keyData);
                }

                // RecipientsDao dao = new RecipientsDao();
                if (firstRow.get("nricorpassport").contains("NRIC") || firstRow.get("nricorpassport").contains("NRC")) {
                    String nric = (String) data.get("nric");
                    String recipientsname = (String) data.get("recipientsname");
                    if (!ValidateBatchUtils.isMyanmarNricValid(nric)) {
                        isValid = false;
                        descriptionlist.add(headerDesc.get("nric") + " is invalid");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", "nric");
                        keyData.put("description", headerDesc.get("nric") + " is invalid");
                        keys.add(keyData);
                    }
                    // else if (!nric.matches("\\u1011\\u1001\\u1005")
                    // && dao.isAvailable("nric", nric, "recipientsname", recipientsname)) {
                    // isValid = false;
                    // descriptionlist.add(headerDesc.get("nric") + " already existed");
                    // Map<String, Object> keyData = new HashMap<>();
                    // keyData.put("nric", headerDesc.get("nric") + " already existed");
                    // keyData.put("key", "nric");
                    // keyData.put("description", headerDesc.get("nric") + " already existed");
                    // keys.add(keyData);
                    // }
                    else if (!nric.matches("\\u1011\\u1001\\u1005") && !oldNric.isEmpty() && !oldName.isEmpty()
                            && oldNric.equals(nric) && oldName.equals(recipientsname)) {
                        isValid = false;
                        descriptionlist.add(headerDesc.get("nric") + " is duplicate");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", "nric");
                        keyData.put("description", headerDesc.get("nric") + " is duplicate");
                        keys.add(keyData);
                    }
                    oldNric = nric;
                    oldName = recipientsname;
                } else {
                    String passport = (String) data.get("passport");
                    String nationality = ((String) data.get("nationality")).toLowerCase();
                    if (nationality.isEmpty()) {
                        isValid = false;
                        descriptionlist.add(headerDesc.get("nationality") + " is blank");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", "nationality");
                        keyData.put("description", headerDesc.get("nationality") + " is blank");
                        keys.add(keyData);
                    }
                    // if (!ValidateBatchUtils.isPassportValid(passport)) {
                    // isValid = false;
                    // descriptionlist
                    // .add(headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6
                    // digits");
                    // Map<String, Object> keyData = new HashMap<>();
                    // keyData.put("key", "passport");
                    // keyData.put("description",
                    // headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6
                    // digits");
                    // keys.add(keyData);
                    // }
                    // else if (dao.isAvailable("passport", data.get("passport"), "nationality",
                    // nationality)) {
                    // isValid = false;
                    // descriptionlist.add(headerDesc.get("passport") + " already existed");
                    // Map<String, Object> keyData = new HashMap<>();
                    // keyData.put("passport", headerDesc.get("passport") + " already existed");
                    // keyData.put("key", "passport");
                    // keyData.put("description", headerDesc.get("passport") + " already existed");
                    // keys.add(keyData);
                    // }
                    else if (!oldPassport.isEmpty() && oldPassport.equals(passport)) {
                        isValid = false;
                        descriptionlist.add(headerDesc.get("passport") + " is duplicate");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", "passport");
                        keyData.put("description", headerDesc.get("passport") + " is duplicate");
                        keys.add(keyData);
                    }
                    oldPassport = passport;
                }

                if (!ValidateBatchUtils.isPhoneValid((String) data.get("mobilephone"))) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("mobilephone") + " must be 09/+959/+95 followed by 7-11 digits");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "mobilephone");
                    keyData.put("description",
                            headerDesc.get("mobilephone") + " must be 09/+959/+95 followed by 7-11 digits");
                    keys.add(keyData);
                }
                // if (!ValidateBatchUtils.isDivisionValid((String) data.get("division"))) {
                // isValid = false;
                // descriptionlist.add(headerDesc.get("division")
                // + " must be one of those values (KACHIN, KAYAR, KAYIN, CHIN,
                // SAGAING,TANINTHARYI, BAGO,BAGO(WEST), MAGWE, MANDALAY, MON, RAKHAING,YANGON,
                // SHAN(SOUNTH), SHAN(NORTH), SHAN(EAST), AYARWADDY, NAYPYITAW)");
                // Map<String, Object> keyData = new HashMap<>();
                // keyData.put("key", "division");
                // keyData.put("description", headerDesc.get("division")
                // + " must be one of those values (KACHIN, KAYAR, KAYIN, CHIN,
                // SAGAING,TANINTHARYI, BAGO,BAGO(WEST), MAGWE, MANDALAY, MON, RAKHAING,YANGON,
                // SHAN(SOUNTH), SHAN(NORTH), SHAN(EAST), AYARWADDY, NAYPYITAW)");
                // keys.add(keyData);
                // }

                Map<String, Object> newData = new HashMap<>();
                newData.put("isvalid", true);
                newData.put("errorkeylist", keys);
                newData.putAll(data);
                Map<String, Object> obj = new HashMap<>();
                obj.put("errorlist", keys);
                Map<String, Object> args = new HashMap<>();
                // args.put("serialno", data.get("serialno").split("\\.")[0]);
                args.put("recordstatus", isValid ? 1 : 0);
                args.put("errorcolumn", new ObjectMapper().writeValueAsString(obj));
                // args.put("duration",
                // String.valueOf((double) Duration.between(detailstart,
                // Instant.now()).toMillis() / 1000) + "s");
                new BatchUploadDao().updateBatchDetail(data.get("serialno").split("\\.")[0], Long.parseLong(syskey),
                        args);
                if (isValid) {
                    excludeList.add(row);
                } else {
                    info.setDescriptionlist(descriptionlist);
                    result.setValid(false);
                    info.setLinenumber(Integer.parseInt(data.get("serialno").split("\\.")[0]));
                    infolist.add(info);
                    newData.put("isvalid", false);
                }
                datalist.add(newData);

                context.getLogger()
                        .info(batch.get("batchrefcode") + " validating row " + String.valueOf(i + 1) + " completed");
                boolean isEmpty = true;
                for (String header : headers) {
                    if (!((String) data.get(header)).isEmpty()) {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty)
                    i++;
            }
            result.setInfos(infolist);
            result.setDatalist(datalist);
            for (Row row : excludeList) {
                sheet.removeRow(row);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            String filename = (String) batch.get("batchrefcode") + "-error.xls";
            String xlsxurl = AzureBlobUtils.createBlob(out, filename);
            String txtUrl = writeToText(result, batch);

            // EasyData<BaseResponse> easyData = new EasyData<>(new BaseResponse());
            // Map<String, Object> res = easyData.toMap();
            // res.put("isvalid", result.isValid());
            // res.put("retmessage", result.isValid() ? ServerMessage.SUCCESS
            // : String.valueOf(result.getInValidDatalist().size()) + " records not
            // valid!");

            // res.put("errorexcelurl", url);
            // res.put("errortxturl", txtUrl);

            Map<String, Object> args = new HashMap<>();
            args.put("syskey", syskey);
            args.put("recordstatus", 10);
            args.put("errorfileurl", xlsxurl);
            args.put("errorlogurl", txtUrl);
            args.put("validcount", result.getValidDatalist().size());
            args.put("invalidcount", result.getInValidDatalist().size());
            args.put("verifieddate", Instant.now().toString());
            Duration timeElapsed = Duration.between(start, Instant.now());

            args.put("duration", (double) timeElapsed.toMillis() / 60000);
            new BatchUploadDao().updateBatch(args);
            context.getLogger().info("Verifying Batch " + batch.get("batchrefcode") + " finished");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = format.parse(Instant.now().toString());
            Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                    "<div>Dear User,</div>Your Batch has been verified.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch Number: %s<br />Submission Date: %s<br />Thank you for using VRS 2021, Registration System.",
                    batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                    new SimpleDateFormat("dd/MM/yyyy").format(date)), "VRS 2021, Registration System", "VRS");
            return request.createResponseBuilder(HttpStatus.OK).body(new BaseResponse()).build();

        } catch (Exception e) {
            // try {
            // Map<String, Object> args = new HashMap<>();
            // String id = request.getQueryParameters().get("id");
            // args.put("syskey", id);
            // args.put("recordstatus", 500);
            // new BatchUploadDao().updateBatch(args);
            // } catch (SQLException e1) {
            // context.getLogger().severe(e.getMessage());
            // BaseResponse res = new BaseResponse();
            // res.setRetcode(ServerStatus.SERVER_ERROR);
            // res.setRetmessage(ServerMessage.SERVER_ERROR);
            // return
            // request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
            // }
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }

    private String writeToText(ValidationResult result, Map<String, Object> batch) throws IOException {
        String resultStr = "";
        if (result.isValid()) {
            resultStr = "All Records valid!";
        }
        for (ResultInfo info : result.getInfos()) {
            resultStr += String.format("Record No %s is invalid. %s\n", info.getLinenumber(),
                    String.join(". ", info.getDescriptionlist()));
        }
        String batchno = ((String) batch.get("batchrefcode")).split("-")[1];
        String filename = batchno + "_invalid.txt";
        return AzureBlobUtils.createBlob(new String(Base64.getEncoder().encode(resultStr.getBytes())), filename);
    }

}
