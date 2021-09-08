package com.dvc.functions.batchupload;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dvc.constants.CommonConstants;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.Converter;
import com.dvc.utils.EasySql;
import com.dvc.utils.ExcelUtil;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.LanguageUtils;
import com.dvc.utils.Sender;
import com.dvc.utils.ValidateBatchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.myanmartools.ZawgyiDetector;
import com.itextpdf.text.pdf.fonts.otf.Language;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Azure Functions with HTTP Trigger.
 */
public class VerifyBatchV2 {
    /**
     * This function listens at endpoint "/api/VerifyBatchV2". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/VerifyBatchV2 2. curl {your
     * host}/api/VerifyBatchV2?name=HTTP%20Query
     */
    @FunctionName("verifybatchv2")
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

            // int status = Integer.parseInt((String) batch.get("recordstatus"));
            // if (status >= 20) {
            // BaseResponse res = new BaseResponse();
            // res.setRetcode(ServerStatus.INVALID_REQUEST);
            // res.setRetmessage(ServerMessage.INVALID_REQUEST);
            // return
            // request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            // }

            final String now = Instant.now().toString();

            String url = batch.get("fileurl") + "?" + AzureBlobUtils.getSasToken();
            boolean isOldFormat = false;
            Map<String, String> headerDesc = ExcelUtil.getExcelFirstRow(new URL(url).openStream(),
                    CommonConstants.HEADERS_MOHS);
            List<String> headers = CommonConstants.HEADERS_MOHS;
            if (!headerDesc.get("nrctype").contains("အမျိုးအစား")) {
                isOldFormat = true;
                headerDesc = ExcelUtil.getExcelFirstRow(new URL(url).openStream(), CommonConstants.HEADERS_MOHS_OLD);
                headers = CommonConstants.HEADERS_MOHS_OLD;
            }

            Workbook workbook = new XSSFWorkbook(new URL(url).openStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<Map<String, Object>> datalist = new ArrayList<>();
            ValidationResult result = new ValidationResult();
            List<ResultInfo> infolist = new ArrayList<>();
            ResultInfo info = new ResultInfo();
            int i = 0;
            for (Row row : sheet) {
                int j = 0;
                if (i > 0) {
                    Map<String, Object> map = new HashMap<>();
                    boolean isempty = true;
                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        if (j > headers.size()) {
                            break;
                        }
                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                        try {
                            if (!getCellValue(cell).isEmpty()) {
                                isempty = false;
                            }
                            if (j < headers.size()) {
                                map.put(headers.get(j), getCellValue(cell, workbook, headers.get(j)));
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            if (j < headers.size()) {
                                map.put(headers.get(j), "");
                            }
                        }
                        j++;
                    }
                    while (j < headers.size()) {
                        map.put(headers.get(j), "");
                        j++;
                    }
                    if (!isempty) {
                        Map<String, Object> m = map;

                        String str = (String) m.get("serialno");

                        if (str.isEmpty()) {
                            break;
                        }
                        try {
                            str = LanguageUtils.toEngNum(str);
                            if (str.contains("-")) {
                                str = (String) m.get("serialno");
                            }
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

                            m.put("vaccinationstatus", 0);
                            m.put("piref", ""); // recheck
                            m.put("batchuploadsyskey", syskey);
                            m.put("partnersyskey", batch.get("partnersyskey"));
                            m.put("dob", ValidateBatchUtils.normalizeDob((String) m.get("dob")));
                            m.put("batchrefcode", String.format("%s-%s", batch.get("batchrefcode"), serialno));
                            context.getLogger().info(batch.get("batchrefcode") + " Reading serialno " + serialno);

                            List<String> descriptionlist = new ArrayList<>();
                            boolean isValid = true;
                            List<Map<String, Object>> keys = new ArrayList<>();

                            for (String key : Arrays.asList("recipientsname", "fathername")) {
                                // "nameicpp", "division", "township", "address1"
                                if (((String) m.get(key)).isEmpty()) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get(key).trim() + " is blank");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", key);
                                    keyData.put("description", headerDesc.get(key).trim() + " is blank");
                                    keys.add(keyData);
                                }
                            }

                            String occupation = (String) m.get("occupation");
                            occupation = occupation.split("\\.")[0].trim();
                            m.put("occupation", occupation);
                            if (occupation.isEmpty() || Integer.parseInt(occupation) < 1
                                    || Integer.parseInt(occupation) > 23) {
                                isValid = false;
                                descriptionlist.add(headerDesc.get("occupation") + " is invalid!");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "occupation");
                                keyData.put("description", headerDesc.get("occupation") + " is invalid!");
                                keys.add(keyData);
                            }

                            if (!ValidateBatchUtils.isGenderValid(((String) m.get("gender")))) {
                                isValid = false;
                                descriptionlist.add(headerDesc.get("gender") + " is invalid!");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "gender");
                                keyData.put("description", headerDesc.get("gender") + " is invalid!");
                                keys.add(keyData);
                            }

                            if (!ValidateBatchUtils.isDobValid((String) m.get("dob"))) {
                                isValid = false;
                                descriptionlist
                                        .add(headerDesc.get("dob") + " must be DD/MM/YYYY or not under 18 years");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "dob");
                                keyData.put("description",
                                        headerDesc.get("dob") + " must be DD/MM/YYYY or not under 18 years");
                                keys.add(keyData);
                            }

                            String prefixnrc = (String) m.get("prefixnrc");
                            String nrccode = (String) m.get("nrccode");
                            String nrctype = "";
                            if (!isOldFormat) {
                                nrctype = (String) m.get("nrctype");
                            }
                            String nrcno = (String) m.get("nrcno");
                            nrcno = nrcno.split("\\.")[0];

                            if (prefixnrc.trim().isEmpty() && nrccode.trim().isEmpty() && nrctype.trim().isEmpty()
                                    && nrcno.trim().isEmpty()) {
                                m.put("nric", "");
                            } else {
                                String[] prefixnrcarr = prefixnrc.split("_");
                                String nric = "";
                                if (prefixnrcarr.length == 2) {
                                    nric += prefixnrcarr[1];
                                } else {
                                    nric += prefixnrc;
                                }
                                boolean isPrefixNrc = CommonConstants.PREFIX_NRCS.contains(prefixnrc.trim());
                                if (!isPrefixNrc) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("prefixnrc") + " is invalid!");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", "nric");
                                    keyData.put("description", headerDesc.get("prefixnrc") + " is invalid");
                                    keys.add(keyData);
                                }

                                if (isPrefixNrc
                                        && !CommonConstants.getNrcCodes(prefixnrc.trim()).contains(nrccode.trim())) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("nrccode") + " is invalid!");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", "nric");
                                    keyData.put("description", headerDesc.get("nrccode") + " is invalid");
                                    keys.add(keyData);
                                }
                                if (!nrccode.trim().isEmpty()) {
                                    nric += "/" + nrccode;
                                }

                                if (!isOldFormat && !CommonConstants.NATIONALITY_CODES.contains(nrctype.trim())) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("nrctype") + " is invalid!");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", "nric");
                                    keyData.put("description", headerDesc.get("nrctype") + " is invalid");
                                    keys.add(keyData);
                                }
                                if (!nrctype.trim().isEmpty()) {
                                    nric += "(" + nrctype + ")";
                                }

                                if (!nrcno.trim().matches("^[0-9\\u1040-\\u1049]{5,6}$")) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("nrcno") + " is invalid!");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", "nric");
                                    keyData.put("description", headerDesc.get("nrcno") + " is invalid");
                                    keys.add(keyData);
                                }
                                nric += nrcno;
                                m.put("nric", nric);
                            }

                            String mobilephone = (String) m.get("mobilephone");
                            if (!mobilephone.trim().isEmpty() && !ValidateBatchUtils.isPhoneValid(mobilephone.trim())) {
                                isValid = false;
                                descriptionlist.add(
                                        headerDesc.get("mobilephone") + " must be 09/+959/+95 followed by 7-11 digits");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "mobilephone");
                                keyData.put("description",
                                        headerDesc.get("mobilephone") + " must be 09/+959/+95 followed by 7-11 digits");
                                keys.add(keyData);
                            }

                            String division = (String) m.get("division");
                            boolean isDivision = CommonConstants.DIVISIONS.contains(division.trim());
                            if (!isDivision) {
                                isValid = false;
                                descriptionlist.add(headerDesc.get("division") + " is invalid!");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "division");
                                keyData.put("description", headerDesc.get("division") + " is invalid!");
                                keys.add(keyData);

                                isValid = false;
                                descriptionlist.add(headerDesc.get("township") + " is invalid!");
                                keyData = new HashMap<>();
                                keyData.put("key", "township");
                                keyData.put("description", headerDesc.get("township") + " is invalid!");
                                keys.add(keyData);

                                isValid = false;
                                descriptionlist.add(headerDesc.get("ward") + " is invalid!");
                                keyData = new HashMap<>();
                                keyData.put("key", "ward");
                                keyData.put("description", headerDesc.get("ward") + " is invalid!");
                                keys.add(keyData);
                            }

                            String township = (String) m.get("township");
                            String ward = (String) m.get("ward");
                            boolean isTownship = true;
                            if (isDivision) {
                                isTownship = CommonConstants.getTownships(division.trim()).contains(township.trim());
                                if (!isTownship) {
                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("township") + " is invalid!");
                                    Map<String, Object> keyData = new HashMap<>();
                                    keyData.put("key", "township");
                                    keyData.put("description", headerDesc.get("township") + " is invalid!");
                                    keys.add(keyData);

                                    isValid = false;
                                    descriptionlist.add(headerDesc.get("ward") + " is invalid!");
                                    keyData = new HashMap<>();
                                    keyData.put("key", "ward");
                                    keyData.put("description", headerDesc.get("ward") + " is invalid!");
                                    keys.add(keyData);
                                }

                            }

                            if (isTownship && isDivision && !CommonConstants.getWards(division.trim(), township.trim())
                                    .contains(ward.trim())) {
                                isValid = false;
                                descriptionlist.add(headerDesc.get("ward") + " is invalid!");
                                Map<String, Object> keyData = new HashMap<>();
                                keyData.put("key", "ward");
                                keyData.put("description", headerDesc.get("ward") + " is invalid!");
                                keys.add(keyData);
                            }

                            String street = (String) m.get("street");

                            String address1 = String.join("၊ ", Arrays.asList(street, ward, division, township));
                            m.put("address1", address1);

                            m.put("recordstatus", isValid ? "1" : "0");
                            Map<String, Object> obj = new HashMap<>();
                            obj.put("errorlist", keys);

                            if (!isValid) {
                                info.setDescriptionlist(descriptionlist);
                                result.setValid(false);
                                info.setLinenumber(Integer.parseInt(((String) m.get("serialno")).split("\\.")[0]));
                                infolist.add(info);
                            }

                            m.put("errorcolumn", new ObjectMapper().writeValueAsString(obj));
                            datalist.add(m);
                            context.getLogger().info(
                                    batch.get("batchrefcode") + " validating row " + String.valueOf(i) + " completed");
                        } catch (Exception rowe) {
                            context.getLogger().severe(rowe.getMessage());
                        }
                    } else {
                        break;
                    }
                }
                i++;
            }
            workbook.close();
            result.setInfos(infolist);
            result.setDatalist(datalist);
            String txtUrl = writeToText(result, batch);
            new EasySql(DbFactory.getConnection()).deleteOne("BatchDetails", "batchuploadsyskey", syskey);
            new EasySql(DbFactory.getConnection()).insertMany("BatchDetails", datalist);
            Map<String, Object> newbatch = new HashMap<>();
            newbatch.put("errorfileurl", "");
            newbatch.put("errorlogurl", txtUrl);
            newbatch.put("syskey", syskey);
            newbatch.put("uploadedrecords", datalist.size());
            newbatch.put("recordstatus", 10);
            newbatch.put("validcount", datalist.stream().filter(d -> d.get("recordstatus").equals("1"))
                    .collect(Collectors.toList()).size());
            newbatch.put("invalidcount", datalist.stream().filter(d -> d.get("recordstatus").equals("0"))
                    .collect(Collectors.toList()).size());
            newbatch.put("verifieddate", Instant.now().toString());
            Duration timeElapsed = Duration.between(start, Instant.now());
            newbatch.put("duration", (double) timeElapsed.toMillis() / 60000);
            new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", newbatch);
            context.getLogger().info(batch.get("batchrefcode")
                    + " Writing batch detail to database with total records: " + String.valueOf(datalist.size()));
            context.getLogger().info("Verifying Batch " + batch.get("batchrefcode") + " finished");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = format.parse(Instant.now().toString());
            Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                    "<div>Dear User,</div>Your Batch has been verified.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch Number: %s<br />Submission Date: %s<br />Thank you for using VRS 2021, Registration System.",
                    batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                    new SimpleDateFormat("dd/MM/yyyy").format(date)), "VRS 2021, Registration System", "VRS");
            return request.createResponseBuilder(HttpStatus.OK).body(new BaseResponse()).build();
        } catch (Exception e) {
            try {
                Map<String, Object> args = new HashMap<>();
                args.put("syskey", request.getQueryParameters().get("id"));
                args.put("recordstatus", 500);
                new BatchUploadDao().updateBatch(args);
            } catch (Exception e3) {
                context.getLogger().severe(e3.getMessage());
            }

            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }

    private static String getCellValue(Cell cell, Workbook workbook) {
        ZawgyiDetector detector = new ZawgyiDetector();
        try {
            // System.out.println(workbook.getFontAt(cell.getCellStyle().getFontIndex()).getFontName());
            // if (cell.getCellType() != CellType.NUMERIC
            // &&
            // workbook.getFontAt(cell.getCellStyle().getFontIndex()).getFontName().contains("Zawgyi"))
            // {
            // return Converter.zg12uni51(cell.getStringCellValue());
            // }
            if (cell.getCellType() != CellType.NUMERIC
                    && detector.getZawgyiProbability(cell.getStringCellValue()) > 0.999) {
                // System.out.println("Processing cell" + String.valueOf(cell.getColumnIndex() +
                // 1));
                return Converter.zg12uni51(cell.getStringCellValue());
            } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = cell.getDateCellValue();
                    return df.format(date);
                }
            }
            // System.out.println("Processing cell" + String.valueOf(cell.getColumnIndex() +
            // 1));
            return cell.getStringCellValue();
        } catch (Exception e) {
            // System.out.println("Processing cell" + String.valueOf(cell.getColumnIndex() +
            // 1));
            try {
                return Double.toString(cell.getNumericCellValue());
            } catch (Exception e2) {
                return cell.getStringCellValue();
            }
            // return Double.toString(cell.getNumericCellValue());
        }
    }

    private static String getCellValue(Cell cell, Workbook workbook, String key) {
        if (key.equals("township")) {
            try {
                if (cell.getCellType() != CellType.NUMERIC) {
                    return cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = cell.getDateCellValue();
                        return df.format(date);
                    }
                }
                return cell.getStringCellValue();
            } catch (Exception e) {
                try {
                    return Double.toString(cell.getNumericCellValue());
                } catch (Exception e2) {
                    return cell.getStringCellValue();
                }
            }
        } else {
            return getCellValue(cell, workbook);
        }
    }

    private static String getCellValue(Cell cell) {
        ZawgyiDetector detector = new ZawgyiDetector();
        try {
            if (cell.getCellType() != CellType.NUMERIC
                    && detector.getZawgyiProbability(cell.getStringCellValue()) > 0.999) {
                return Converter.zg12uni51(cell.getStringCellValue());
            }
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = cell.getDateCellValue();
                    return df.format(date);
                }
            }

            return cell.getStringCellValue();
        } catch (Exception e) {
            try {
                return Double.toString(cell.getNumericCellValue());
            } catch (Exception e2) {
                return cell.getStringCellValue();
            }
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
