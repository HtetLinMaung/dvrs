package com.dvc.functions.batchupload.quene;

import com.microsoft.azure.functions.annotation.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dvc.constants.CommonConstants;
import com.dvc.dao.BatchUploadDao;
import com.dvc.factory.DbFactory;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.EasySql;
import com.dvc.utils.ExcelUtil;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.ValidateBatchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class InsertVerifyBatch {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("insertverifybatch")
    public void run(
            @QueueTrigger(name = "message", queueName = "insert-verify-batch", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        try {
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(message));

            final String now = Instant.now().toString();

            String url = batch.get("fileurl") + "?" + AzureBlobUtils.getSasToken();
            Map<String, String> firstRow = ExcelUtil.getExcelFirstRow(new URL(url).openStream(),
                    CommonConstants.HEADERS);

            List<Map<String, Object>> detaillist = ExcelUtil.excelToDataList(new URL(url).openStream(),
                    firstRow.get("nricorpassport").contains("NRIC") ? CommonConstants.HEADERS_M
                            : CommonConstants.HEADERS_F)
                    .stream().map(m -> {
                        String str = (String) m.get("serialno");
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
                        m.put("batchuploadsyskey", message);
                        m.put("partnersyskey", batch.get("partnersyskey"));
                        // m.put("pisysKey", dto.getPisyskey());
                        // m.put("batchrefcode", String.format("%s-%s", , serialno));
                        m.put("batchrefcode", String.format("%s-%s", batch.get("batchrefcode"), serialno));
                        return m;
                    }).collect(Collectors.toList());

            new EasySql(DbFactory.getConnection()).deleteOne("BatchDetails", "batchuploadsyskey", message);
            new EasySql(DbFactory.getConnection()).insertMany("BatchDetails", detaillist);
            Map<String, Object> newbatch = new HashMap<>();
            newbatch.put("syskey", message);
            newbatch.put("uploadedrecords", detaillist.size());
            new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", newbatch);
            context.getLogger()
                    .info("Writing batch detail to database with total records: " + String.valueOf(detaillist.size()));

            List<String> headers = firstRow.get("nricorpassport").contains("NRIC") ? CommonConstants.HEADERS_M
                    : CommonConstants.HEADERS_F;
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

                List<String> descriptionlist = new ArrayList<>();
                boolean isValid = true;
                List<Map<String, Object>> keys = new ArrayList<>();
                ResultInfo info = new ResultInfo();

                Map<String, String> data = ExcelUtil.getMapFromRow(row, headers);
                for (String key : Arrays.asList("serialno", "recipientsname", "fathername", "township")) {
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
                if (firstRow.get("nricorpassport").contains("NRIC")) {
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
                    if (!ValidateBatchUtils.isPassportValid(passport)) {
                        isValid = false;
                        descriptionlist
                                .add(headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("key", "passport");
                        keyData.put("description",
                                headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                        keys.add(keyData);
                    }
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
                new BatchUploadDao().updateBatchDetail(data.get("serialno").split("\\.")[0], Long.parseLong(message),
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

                System.out.println("validating row " + String.valueOf(i + 1) + " completed");
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
            for (

            Row row : excludeList) {
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
            args.put("syskey", message);
            args.put("recordstatus", 10);
            args.put("errorfileurl", xlsxurl);
            args.put("errorlogurl", txtUrl);
            new BatchUploadDao().updateBatch(args);
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
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
