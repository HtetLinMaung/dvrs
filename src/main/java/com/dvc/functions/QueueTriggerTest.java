package com.dvc.functions;

import com.microsoft.azure.functions.annotation.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dvc.constants.CommonConstants;
import com.dvc.constants.ServerMessage;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.models.BaseResponse;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.EasyData;
import com.dvc.utils.ExcelUtil;
import com.dvc.utils.ValidateBatchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QueueTriggerTest {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("QueueTriggerTest")
    public void run(
            @QueueTrigger(name = "message", queueName = "myquene", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        System.out.println("myquene is trigger");
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        System.out.println("fetching batch by syskey...");

        System.out.println("fetching batch by syskey completed");

        // int status = Integer.parseInt((String) batch.get("recordstatus"));
        // if (status >= 20) {
        // BaseResponse res = new BaseResponse();
        // res.setRetcode(ServerStatus.INVALID_REQUEST);
        // res.setRetmessage(ServerMessage.INVALID_REQUEST);
        // return
        // request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
        // }

        try {
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(message));
            String fileurl = (String) batch.get("fileurl");
            Map<String, Object> res = performanceFunction(fileurl, batch, Long.parseLong(message));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // return request.createResponseBuilder(HttpStatus.OK).body(res).build();
    }

    private Map<String, Object> performanceFunction(String fileurl, Map<String, Object> batch, long syskey)
            throws IOException, SQLException {
        List<String> headers = fileurl.contains("M-") ? CommonConstants.HEADERS_M : CommonConstants.HEADERS_F;
        Map<String, String> headerDesc = ExcelUtil.getExcelFirstRow(new URL(fileurl).openStream(), headers);
        ValidationResult result = new ValidationResult();
        List<ResultInfo> infolist = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(new URL(fileurl).openStream());
        Sheet sheet = workbook.getSheetAt(0);
        String oldNric = "";
        String oldPassport = "";
        List<Row> excludeList = new ArrayList<>();
        List<Map<String, Object>> datalist = new ArrayList<>();

        int i = 0;
        int j = 1;
        for (Row row : sheet) {
            if (j == 1) {
                j++;
                continue;
            }
            if (Integer.parseInt(((String) batch.get("uploadedrecords")).split("\\.")[0]) == i)
                break;

            List<String> descriptionlist = new ArrayList<>();
            boolean isValid = true;
            List<Map<String, Object>> keys = new ArrayList<>();
            ResultInfo info = new ResultInfo();

            Map<String, String> data = ExcelUtil.getMapFromRow(row, headers);
            for (String key : Arrays.asList("serialno", "recipientsname", "fathername")) {
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
                descriptionlist.add(headerDesc.get("dob") + " must be DD/MM/YYYY");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "dob");
                keyData.put("description", headerDesc.get("dob") + " must be DD/MM/YYYY");
                keys.add(keyData);
            }

            RecipientsDao dao = new RecipientsDao();
            if (fileurl.contains("M-")) {
                String nric = (String) data.get("nric");
                if (!ValidateBatchUtils.isMyanmarNricValid(nric)) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("nric") + " is invalid");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "nric");
                    keyData.put("description", headerDesc.get("nric") + " is invalid");
                    keys.add(keyData);
                } else if (!nric.matches("\\u1011\\u1001\\u1005") && dao.isAvailable("nric", nric)) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("nric") + " already existed");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("nric", headerDesc.get("nric") + " already existed");
                    keyData.put("key", "nric");
                    keyData.put("description", headerDesc.get("nric") + " already existed");
                    keys.add(keyData);
                } else if (!nric.matches("\\u1011\\u1001\\u1005") && !oldNric.isEmpty() && oldNric.equals(nric)) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("nric") + " is duplicate");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "nric");
                    keyData.put("description", headerDesc.get("nric") + " is duplicate");
                    keys.add(keyData);
                }
                oldNric = nric;
            } else {
                String passport = (String) data.get("passport");
                String nationality = ((String) data.get("nationality")).toLowerCase();
                if (!ValidateBatchUtils.isPassportValid(passport)) {
                    isValid = false;
                    descriptionlist
                            .add(headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "passport");
                    keyData.put("description",
                            headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    keys.add(keyData);
                } else if (dao.isAvailable("passport", data.get("passport"), "nationality", nationality)) {
                    isValid = false;
                    descriptionlist.add(headerDesc.get("passport") + " already existed");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("passport", headerDesc.get("passport") + " already existed");
                    keyData.put("key", "passport");
                    keyData.put("description", headerDesc.get("passport") + " already existed");
                    keys.add(keyData);
                } else if (!oldPassport.isEmpty() && oldPassport.equals(passport)) {
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
                descriptionlist.add(headerDesc.get("mobilephone") + " must be 09 followed by 7-9 digits");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "mobilephone");
                keyData.put("description", headerDesc.get("mobilephone") + " must be 09 followed by 7-9 digits");
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
            args.put("serialno", data.get("serialno").split("\\.")[0]);
            args.put("recordstatus", isValid ? 1 : 0);
            args.put("errorcolumn", new ObjectMapper().writeValueAsString(obj));
            new BatchUploadDao().updateBatchDetail("serialno", args);
            if (isValid) {
                excludeList.add(row);
            } else {
                info.setDescriptionlist(descriptionlist);
                result.setValid(false);
                info.setLinenumber(i);
                infolist.add(info);
                newData.put("isvalid", false);
            }
            datalist.add(newData);

            System.out.println("validating row " + String.valueOf(i + 1) + " completed");
            boolean isEmpty = true;
            for (String header : CommonConstants.HEADERS) {
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
        String url = AzureBlobUtils.createBlob(out, filename);
        String txtUrl = writeToText(result, batch);

        EasyData<BaseResponse> easyData = new EasyData<>(new BaseResponse());
        Map<String, Object> res = easyData.toMap();
        res.put("isvalid", result.isValid());
        res.put("retmessage", result.isValid() ? ServerMessage.SUCCESS
                : String.valueOf(result.getInValidDatalist().size()) + " records not valid!");

        res.put("errorexcelurl", url);
        res.put("errortxturl", txtUrl);

        Map<String, Object> args = new HashMap<>();
        args.put("syskey", syskey);
        args.put("recordstatus", 10);
        args.put("errorfileurl", url);
        args.put("errorlogurl", txtUrl);
        new BatchUploadDao().updateBatch(args);
        return res;
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
