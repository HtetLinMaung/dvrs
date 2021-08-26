package com.dvc.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dvc.constants.CommonConstants;
import com.dvc.factory.DbFactory;
import com.dvc.models.BatchDto;
import com.dvc.models.ComboData;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.QRYData;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.Cid;
import com.dvc.utils.EasySql;
import com.dvc.utils.ExcelUtil;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.QRNewUtils;

import com.dvc.utils.ValidateBatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.storage.StorageException;

public class BatchUploadDao extends BaseDao implements IBatchUploadDao {

    // private String generateBatchRef(long partnerSyskey) throws SQLException {
    // String batchRef = String.valueOf(KeyGenerator.generateID()) + "-00001";

    // List<Map<String, Object>> datalist = new
    // EasySql(DbFactory.getConnection()).getMany(
    // Arrays.asList("BatchRefCode"), "BatchUpload where recordstatus >= 20 and
    // PartnerSysKey = ?",
    // Arrays.asList(partnerSyskey));
    // if (datalist.size() > 0) {
    // int max = 0;
    // for (Map<String, Object> data : datalist) {
    // String ref = (String) data.get("BatchRefCode");
    // int refnumber = Integer.parseInt(ref.split("-")[1]);
    // if (refnumber > max) {
    // max = refnumber;
    // }
    // }
    // String newBatchRef = (String) datalist.get(0).get("BatchRefCode");
    // String subBatch = String.format("%05d", max + 1);
    // return newBatchRef.split("-")[0] + "-" + subBatch;
    // }

    // return batchRef;
    // }

    private List<Map<String, Object>> getDetailList(long syskey, BatchDto dto) throws IOException {
        final String now = Instant.now().toString();

        Map<String, String> firstRow = ExcelUtil.getExcelFirstRow(dto.getFile(), CommonConstants.HEADERS);

        return ExcelUtil.excelToDataList(dto.getFile(),
                firstRow.get("nricorpassport").contains("NRIC") ? CommonConstants.HEADERS_M : CommonConstants.HEADERS_F)
                .stream().map(m -> {
                    String str = (String) m.get("serialno");
                    String serialno = "0";
                    if (str.split("\\.").length > 0) {
                        serialno = str.split("\\.")[0];
                    }
                    m.put("serialno", serialno);
                    m.put("syskey", KeyGenerator.generateSyskey());
                    m.put("createddate", now);
                    m.put("modifieddate", now);
                    m.put("recordstatus", 0);
                    m.put("vaccinationstatus", 0);
                    m.put("piref", ""); // recheck
                    m.put("batchuploadsyskey", syskey);
                    m.put("partnersyskey", dto.getPartnersyskey());
                    m.put("pisysKey", dto.getPisyskey());
                    m.put("mobilephone", ValidateBatchUtils.normalizePhone((String) m.get("mobilephone")));
                    // m.put("batchrefcode", String.format("%s-%s", , serialno));
                    return m;
                }).collect(Collectors.toList());
    }

    private String getBatchRefCode(String filename) {
        return filename.split("-")[1] + "-" + filename.split("-")[2];
    }

    private int getNewBatchNo(long partnersyskey) throws SQLException {
        return getTotalCount("BatchUpload where partnersyskey = ?", Arrays.asList(partnersyskey)) + 1;
    }

    public long saveBatchHeader(BatchDto dto, Map<String, Object> pi) throws SQLException {
        long syskey = KeyGenerator.generateSyskey();
        String now = Instant.now().toString();
        String batchrefcode = String.format("%s-%03d", dto.getPartnerid().substring(dto.getPartnerid().length() - 3),
                getNewBatchNo(Long.parseLong(dto.getPartnersyskey())));
        List<String> keys = Arrays.asList("syskey", "createddate", "modifieddate", "userid", "username", "UploadedDate",
                "ByUserID", "uploadedrecords", "fileurl", "PartnerSysKey", "PISysKey", "remark", "voidstatus",
                "centerid", "batchrefcode", "recordstatus", "submitteddate", "approveddate", "isblankcard");
        List<Object> args = Arrays.asList(syskey, now, now, dto.getUserid(), dto.getUsername(), now, dto.getUserid(),
                pi.get("qty"), "", dto.getPartnersyskey(), pi.get("syskey"), dto.getRemark(), 1, dto.getCenterid(),
                batchrefcode, 30, now, now, 1);
        int result = getDBClient().insertOne("BatchUpload", keys, args);

        if (result == 0)
            return 0;

        return syskey;
    }

    @Override
    public long saveBatch(BatchDto dto) throws SQLException, IOException {
        final String now = Instant.now().toString();
        long syskey = KeyGenerator.generateSyskey();

        // TODO: later keep old version file
        String fileUrl = AzureBlobUtils.createBlob(dto.getFile(),
                UUID.randomUUID().toString() + "_" + dto.getFilename());

        List<Map<String, Object>> datalist = new ArrayList<>();

        if (!dto.getBatchsyskey().isEmpty()) {
            syskey = Long.parseLong(dto.getBatchsyskey());
            // Map<String, Object> batch =
            // getDBClient().getOne(Arrays.asList("batchrefcode"),
            // "BatchUpload where syskey = ?", Arrays.asList(syskey));
            // datalist = getDetailList(syskey, dto).stream().map(m -> {
            // m.put("batchrefcode", String.format("%s-%s", batch.get("batchrefcode"),
            // m.get("serialno")));
            // return m;
            // }).collect(Collectors.toList());
            Map<String, Object> data = new HashMap<>();
            data.put("syskey", syskey);
            data.put("recordstatus", 1);
            data.put("filename", dto.getFilename());
            data.put("fileurl", fileUrl);
            data.put("modifieddate", now);
            data.put("UploadedDate", now);
            data.put("userid", dto.getUserid());
            data.put("username", dto.getUsername());
            data.put("remark", dto.getRemark());
            data.put("UploadedRecords", datalist.size());
            data.put("validcount", 0);
            data.put("invalidcount", 0);
            int result = getDBClient().updateOne("BatchUpload", "syskey", data);
            if (result == 0) {
                return 0;
            }
        } else {
            // datalist = getDetailList(syskey, dto);
            String batchrefcode = String.format("%s-%03d",
                    dto.getPartnerid().substring(dto.getPartnerid().length() - 3),
                    getNewBatchNo(Long.parseLong(dto.getPartnersyskey())));
            List<String> keys = Arrays.asList("syskey", "createddate", "modifieddate", "userid", "username",
                    "UploadedDate", "ByUserID", "uploadedrecords", "fileurl", "PartnerSysKey", "PISysKey", "remark",
                    "voidstatus", "centerid", "batchrefcode", "filename");
            List<Object> args = Arrays.asList(syskey, now, now, dto.getUserid(), dto.getUsername(), now,
                    dto.getUserid(), datalist.size(), fileUrl, dto.getPartnersyskey(), 0, dto.getRemark(), 1,
                    dto.getCenterid(), batchrefcode, dto.getFilename());
            int result = getDBClient().insertOne("BatchUpload", keys, args);
            if (result == 0)
                return 0;

            // Map<String, Object> batch = getDBClient().getOne(Arrays.asList("batchno",
            // "syskey"),
            // "BatchUpload where syskey = ?", Arrays.asList(syskey));
            // String batchrefcode = String.format("%s-%03d",
            // dto.getPartnerid().substring(dto.getPartnerid().length() - 3),
            // Integer.parseInt((String) batch.get("batchno")));
            // batch.put("batchrefcode", batchrefcode);
            // batch.remove("batchno");
            // result = getDBClient().updateOne("BatchUpload", "syskey", batch);
            // if (result == 0)
            // return 0;

            // datalist = datalist.stream().map(m -> {
            // m.put("batchrefcode", String.format("%s-%s", batchrefcode,
            // m.get("serialno")));
            // return m;
            // }).collect(Collectors.toList());
        }

        // new EasySql(DbFactory.getConnection()).deleteOne("BatchDetails",
        // "batchuploadsyskey", syskey);
        // new EasySql(DbFactory.getConnection()).insertMany("BatchDetails", datalist);

        return syskey;
    }

    @Override
    public void saveRecipents(BatchDto dto, ExecutionContext context) throws SQLException, IOException {
        final String now = Instant.now().toString();

        int currentcount = getTotalCount("Recipients where pisyskey = ? and batchuploadsyskey <> 0",
                Arrays.asList(dto.getPisyskey()));

        Map<String, Object> pi = new PIDao().getPi(Long.parseLong(dto.getPisyskey()));
        int totalcount = Integer.parseInt((String) pi.get("qty"));

        int remainingcount = totalcount - currentcount + Integer.parseInt((String) pi.get("voidcount"));

        // new CenterDao().addLastSerial((String) pi.get("centerid"),
        // Integer.parseInt((String) pi.get("qty")));
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getDatalist().size() <= remainingcount) {
            String sql = "select cid from CenterLastSerials where centerid = ?";
            try (Connection connection = DbFactory.getConnection();
                    PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setObject(1, pi.get("centerid"));
                ResultSet rs = stmt.executeQuery();
                int srno = 0;
                boolean found = false;

                while (rs.next()) {
                    found = true;
                    srno = Cid.getNumberFromCid(rs.getString("cid"));
                    // srno =
                    // Integer.parseInt(rs.getString("cid").replaceAll("^([a-zA-Z]{1,3}[0-9])",
                    // ""));
                }
                int i = 1;
                if (!found) {
                    sql = "insert into CenterLastSerials (syskey, centerid, cid) values (?, ?, ?)";
                    PreparedStatement addstmt = connection.prepareStatement(sql);
                    addstmt.setLong(i++, KeyGenerator.generateSyskey());
                    addstmt.setObject(i++, pi.get("centerid"));
                    addstmt.setString(i++,
                            (String) pi.get("centerid") + String.format("%07d", srno + dto.getDatalist().size()));
                    addstmt.executeUpdate();
                } else {
                    sql = "update CenterLastSerials set cid = ? where centerid = ?";
                    PreparedStatement updatestmt = connection.prepareStatement(sql);
                    updatestmt.setString(i++,
                            (String) pi.get("centerid") + String.format("%07d", srno + dto.getDatalist().size()));
                    updatestmt.setObject(i++, pi.get("centerid"));
                    updatestmt.executeUpdate();
                }

                int j = 1;
                for (Map<String, Object> m : dto.getDatalist()) {
                    String cid = (String) pi.get("centerid") + String.format("%07d", ++srno);
                    Map<String, Object> recipient = new HashMap<>();
                    recipient.put("cid", cid);
                    recipient.put("syskey", KeyGenerator.generateSyskey());
                    recipient.put("createddate", now);
                    recipient.put("modifieddate", now);
                    recipient.put("rid", m.get("recipientid"));
                    String dob = (String) m.get("dob");
                    recipient.put("dob", dob);
                    if (dob.split("/").length == 3) {
                        // recipient.put("dob",
                        // dob.split("/")[2] + String.format("%02d",
                        // Integer.parseInt(dob.split("/")[1]))
                        // + String.format("%02d", Integer.parseInt(dob.split("/")[0])));
                        // int dobyear = Integer.parseInt(dob.split("/")[2]);
                        // int year = LocalDate.now().getYear();
                        recipient.put("age", ValidateBatchUtils.getAge(dob));

                    }

                    recipient.put("batchuploadsyskey", dto.getBatchsyskey());
                    recipient.put("recipientsname", m.get("recipientsname"));
                    recipient.put("nric", m.get("nric"));
                    recipient.put("remark", m.get("remark"));
                    recipient.put("fathername", m.get("fathername"));
                    recipient.put("gender", m.get("gender"));
                    recipient.put("passport", m.get("passport"));
                    recipient.put("nationality", m.get("nationality"));
                    recipient.put("organization", m.get("organization"));
                    recipient.put("address1", m.get("address1"));
                    recipient.put("township", m.get("township"));
                    recipient.put("division", m.get("division"));
                    recipient.put("mobilephone", m.get("mobilephone"));
                    recipient.put("batchrefcode", m.get("batchrefcode"));
                    recipient.put("piref", pi.get("pirefnumber"));
                    recipient.put("pisyskey", dto.getPisyskey());
                    recipient.put("partnersyskey", dto.getPartnersyskey());
                    recipient.put("voidstatus", 1);
                    if (pi.get("centerid").equals("YGN1")) {
                        if (srno <= 6300) {
                            int slot = (int) Math.ceil((double) srno / 350);
                            int day = (int) Math.ceil((double) slot / 6);
                            int timeslot = slot - ((day - 1) * 6);
                            String firstdosetime = "";
                            switch (timeslot) {
                                case 1:
                                    // 8:30
                                    firstdosetime = "8:30 AM";
                                    break;
                                case 2:
                                    // 9:30
                                    firstdosetime = "9:30 AM";
                                    break;
                                case 3:
                                    // 10:30
                                    firstdosetime = "10:30 AM";
                                    break;
                                case 4:
                                    // 1:00
                                    firstdosetime = "1:00 PM";
                                    break;
                                case 5:
                                    // 2:00
                                    firstdosetime = "2:00 PM";
                                    break;
                                case 6:
                                    // 3:00
                                    firstdosetime = "3:00 PM";
                                    break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String date = formatter.format(LocalDate.of(2021, 8, 22).plusDays(day));
                            // if (formatter.format(LocalDate.now()).equals("23/08/2021")) {
                            // date = formatter.format(LocalDate.now().plusDays(day));
                            // }
                            recipient.put("firstdosedate", date);
                            recipient.put("firstdosetime", firstdosetime);
                            recipient.put("seconddosetime", firstdosetime);
                        } else if (srno > 6300 && srno <= 16500) {
                            int slot = (int) Math.ceil((double) (srno - 6300) / 850);
                            int day = (int) Math.ceil((double) slot / 6);
                            int timeslot = slot - ((day - 1) * 6);
                            String firstdosetime = "";
                            switch (timeslot) {
                                case 1:
                                    // 8:30
                                    firstdosetime = "8:30 AM";
                                    break;
                                case 2:
                                    // 9:30
                                    firstdosetime = "9:30 AM";
                                    break;
                                case 3:
                                    // 10:30
                                    firstdosetime = "10:30 AM";
                                    break;
                                case 4:
                                    // 1:00
                                    firstdosetime = "1:00 PM";
                                    break;
                                case 5:
                                    // 2:00
                                    firstdosetime = "2:00 PM";
                                    break;
                                case 6:
                                    // 3:00
                                    firstdosetime = "3:00 PM";
                                    break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String date = formatter.format(LocalDate.of(2021, 8, 25).plusDays(day));
                            // if (formatter.format(LocalDate.now()).equals("23/08/2021")) {
                            // date = formatter.format(LocalDate.now().plusDays(day));
                            // }
                            recipient.put("firstdosedate", date);
                            recipient.put("firstdosetime", firstdosetime);
                            recipient.put("seconddosetime", firstdosetime);
                        } else if (srno > 16500 && srno <= 18600) {
                            int slot = (int) Math.ceil((double) (srno - 16500) / 350);
                            int day = (int) Math.ceil((double) slot / 6);
                            int timeslot = slot - ((day - 1) * 6);
                            String firstdosetime = "";
                            switch (timeslot) {
                                case 1:
                                    // 8:30
                                    firstdosetime = "8:30 AM";
                                    break;
                                case 2:
                                    // 9:30
                                    firstdosetime = "9:30 AM";
                                    break;
                                case 3:
                                    // 10:30
                                    firstdosetime = "10:30 AM";
                                    break;
                                case 4:
                                    // 1:00
                                    firstdosetime = "1:00 PM";
                                    break;
                                case 5:
                                    // 2:00
                                    firstdosetime = "2:00 PM";
                                    break;
                                case 6:
                                    // 3:00
                                    firstdosetime = "3:00 PM";
                                    break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String date = formatter.format(LocalDate.of(2021, 8, 27).plusDays(day));
                            // if (formatter.format(LocalDate.now()).equals("23/08/2021")) {
                            // date = formatter.format(LocalDate.now().plusDays(day));
                            // }
                            recipient.put("firstdosedate", date);
                            recipient.put("firstdosetime", firstdosetime);
                            recipient.put("seconddosetime", firstdosetime);
                        } else if (srno > 18600) {
                            int slot = (int) Math.ceil((double) (srno - 18600) / 700);
                            int day = (int) Math.ceil((double) slot / 6);
                            int timeslot = slot - ((day - 1) * 6);
                            String firstdosetime = "";
                            switch (timeslot) {
                                case 1:
                                    // 8:30
                                    firstdosetime = "8:30 AM";
                                    break;
                                case 2:
                                    // 9:30
                                    firstdosetime = "9:30 AM";
                                    break;
                                case 3:
                                    // 10:30
                                    firstdosetime = "10:30 AM";
                                    break;
                                case 4:
                                    // 1:00
                                    firstdosetime = "1:00 PM";
                                    break;
                                case 5:
                                    // 2:00
                                    firstdosetime = "2:00 PM";
                                    break;
                                case 6:
                                    // 3:00
                                    firstdosetime = "3:00 PM";
                                    break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String date = formatter.format(LocalDate.of(2021, 8, 28).plusDays(day));
                            // if (formatter.format(LocalDate.now()).equals("23/08/2021")) {
                            // date = formatter.format(LocalDate.now().plusDays(day));
                            // }
                            recipient.put("firstdosedate", date);
                            recipient.put("firstdosetime", firstdosetime);
                            recipient.put("seconddosetime", firstdosetime);
                        }

                    } else if (pi.get("centerid").equals("YGN")) {
                        recipient.put("firstdosedate", "");
                        recipient.put("firstdosetime", "8:30 AM");
                        recipient.put("seconddosetime", "8:30 AM");
                    }

                    QRYData data = new QRYData();
                    data.setCid((String) recipient.get("cid"));
                    // data.setDob((String) recipient.get("dob"));
                    // data.setSyskey(String.valueOf(recipient.get("syskey")));
                    // data.setName((String) recipient.get("recipientsname"));
                    // data.setNric((String) recipient.get("nric"));
                    // data.setQraction("url");
                    // data.setUrl("https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/"
                    // + data.getCid());
                    // Map<String, Object> mapdata = new EasyData<QRYData>(data).toMap();
                    // mapdata.put("BTN-Dose",
                    // "https://vrs2021.registrationsystem.org/#/main/dvrs/vaccine/" +
                    // data.getCid());

                    String qrtoken = QRNewUtils.generateQRToken(data);
                    recipient.put("qrtoken", qrtoken);
                    datalist.add(recipient);

                    context.getLogger().info(m.get("batchrefcode") + ":" + cid + " processed completed");
                }

            }
            getDBClient().insertMany("Recipients", datalist);
            String cidrange = new RecipientsDao().getCidRange(Long.parseLong(dto.getBatchsyskey()));
            Map<String, Object> newbatch = new HashMap<>();
            newbatch.put("recipientsaved", 1);
            newbatch.put("syskey", dto.getBatchsyskey());
            newbatch.put("cidrange", cidrange);
            getDBClient().updateOne("BatchUpload", "syskey", newbatch);
            context.getLogger().info(String.valueOf(dto.getDatalist().size()) + " recipients saved successful");
            // CommonUtils.writeFiles(message);
            // Map<String, String> options = new HashMap<>();
            // options.put("Authorization", btoken);
            // Map<String, Object> ret = RestClient.post(
            // "https://apx.registrationsystem.org/api/writeFiles?batchno=" +
            // dto.getBatchsyskey(),
            // new HashMap<>(), options);

            // System.out.println(ret);
        }

        // List<Map<String, Object>> recipients = new RecipientsDao()
        // .getRegisteredRecipients(Long.parseLong(dto.getPisyskey()));

        // if (dto.getDatalist().size() <= recipients.size()) {
        // int i = 0;
        // for (Map<String, Object> m : dto.getDatalist()) {
        // Map<String, Object> recipient = recipients.get(i++);
        // recipient.put("modifieddate", now);
        // recipient.put("rid", m.get("recipientid"));
        // String dob = (String) m.get("dob");
        // recipient.put("dob", dob.split("/")[2] + dob.split("/")[1] +
        // dob.split("/")[0]);
        // int dobyear = Integer.parseInt(dob.split("/")[2]);
        // int year = LocalDate.now().getYear();
        // recipient.put("age", year - dobyear);
        // recipient.put("batchuploadsyskey", dto.getBatchsyskey());
        // recipient.put("recipientsname", m.get("recipientsname"));
        // recipient.put("nric", m.get("nric"));
        // recipient.put("remark", m.get("remark"));
        // recipient.put("fathername", m.get("fathername"));
        // recipient.put("gender", m.get("gender"));
        // recipient.put("passport", m.get("passport"));
        // recipient.put("nationality", m.get("nationality"));
        // recipient.put("organization", m.get("organization"));
        // recipient.put("address1", m.get("address1"));
        // recipient.put("township", m.get("township"));
        // recipient.put("division", m.get("division"));
        // recipient.put("mobilephone", m.get("mobilephone"));
        // recipient.put("batchrefcode", m.get("batchrefcode"));

        // QRYData data = new QRYData();
        // data.setCid((String) recipient.get("cid"));
        // data.setDob((String) recipient.get("dob"));
        // data.setSyskey((String) recipient.get("syskey"));
        // data.setName((String) recipient.get("recipientsname"));
        // data.setNric((String) recipient.get("nric"));
        // data.setQraction("json");
        // String qrtoken = QRUtils.generateQRToken(data);
        // recipient.put("qrtoken", qrtoken);
        // getDBClient().updateOne("Recipients", "syskey", recipient);
        // }
        // }

    }

    @Override
    public List<Map<String, Object>> getBatchDataList(long syskey) throws SQLException, IOException {
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList("FileURL"),
                "BatchUpload where recordstatus <> 4 and syskey = ?", Arrays.asList(syskey));
        String url = (String) map.get("FileURL");
        List<String> headers = Arrays.asList("no", "recipientsname", "nameicpp", "gender", "fathername", "dob", "nric",
                "passport", "nationality", "organization", "mobilephone", "division", "township", "address1", "remark");
        InputStream in = new URL(url).openStream();
        return ExcelUtil.excelToDataList(in, headers);
    }

    @Override
    public int updateBatchStatus(long syskey, int status) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", syskey);
        args.put("recordstatus", status);
        return new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", args);
    }

    public int updateBatchStatus(long syskey, int status, long partnerSyskey) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", syskey);
        args.put("recordstatus", status);
        int result = 0;
        if (status == 20) {
            // args.put("BatchRefCode", generateBatchRef(partnerSyskey));
            args.put("submitteddate", Instant.now().toString());
            result = new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", args);
            // final String sql = "delete from BatchUpload where recordstatus < 20 and
            // partnersyskey = ?";
            // try (Connection connection = DbFactory.getConnection();
            // PreparedStatement stmt = connection.prepareStatement(sql);) {
            // stmt.setLong(1, partnerSyskey);
            // stmt.executeUpdate();
            // }
        }

        return result;
    }

    @Override
    public int getBatchStatus(long syskey) throws SQLException {
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList("recordstatus"),
                "BatchUpload where recordstatus <> 4 and syskey = ?", Arrays.asList(syskey));
        String recordstatus = (String) map.get("recordstatus");
        if (recordstatus.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(recordstatus);
    }

    private String getComboDescription(List<ComboData> datalist, Map<String, Object> m, String key) {
        List<ComboData> filteredList = datalist.stream()
                .filter(com -> (int) com.getValue() == Integer.parseInt((String) m.get(key)))
                .collect(Collectors.toList());
        if (filteredList.size() == 0) {
            return "";
        }
        return filteredList.get(0).getDescription();
    }

    private List<Map<String, Object>> getFormattedBatchDatalist(List<Map<String, Object>> datalist) {

        return datalist.stream().map(m -> {
            // try {
            // m.put("validcount", getTotalCount("BatchDetails WHERE recordstatus = 1 and
            // batchuploadsyskey = ?",
            // Arrays.asList(m.get("syskey"))));
            // m.put("invalidcount", getTotalCount("BatchDetails WHERE recordstatus = 0 and
            // batchuploadsyskey = ?",
            // Arrays.asList(m.get("syskey"))));
            // } catch (SQLException e1) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // }
            m.put("approvaldesc", getComboDescription(CommonConstants.APPROVAL_LIST, m, "recordstatus"));
            // m.put("paymentdesc", getComboDescription(CommonConstants.PAYMENT_LIST, m,
            // "paymentstatus"));
            // m.put("voiddesc", getComboDescription(CommonConstants.VOID_LIST, m,
            // "voidstatus"));
            // if (m.containsKey("attachmenturl1")) {
            // String attachmenturl1 = (String) m.get("attachmenturl1");
            // m.put("attachmentlist", new ArrayList<>());
            // if (attachmenturl1 != null) {
            // try {
            // Map<String, Object> attachmentObj = new
            // ObjectMapper().readValue(attachmenturl1, Map.class);
            // m.put("attachmentlist", attachmentObj.get("datalist"));
            // m.remove("attachmenturl1");
            // } catch (JsonMappingException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (JsonProcessingException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            // }
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public PaginationResponse<Map<String, Object>> getBatchList(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("batchrefcode"), dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("b.syskey", "b.recordstatus", "uploadeddate", "approveddate", "submitteddate",
                "uploadedrecords", "batchrefcode", "voidstatus", "partnername", "centerid", "partnerid", "filename",
                "fileurl", "validcount", "invalidcount", "duration", "b.t3", "cidrange");
        String query = "";
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getRole().equals("Admin") || dto.getRole().equals("Finance")) {
            query = String.format(
                    "BatchUpload as b left join partners as p on p.syskey = b.partnersyskey WHERE b.recordstatus <> 4 %s AND (%s)",
                    dto.getPartnersyskey().isEmpty() ? "" : "and b.partnersyskey = ?", searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "b.createddate", true,
                    dto.getCurrentpage(), dto.getPagesize(),
                    dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        } else {
            query = String.format(
                    "BatchUpload as b left join partners as p on p.syskey = b.partnersyskey WHERE b.recordstatus <> 4 and b.partnersyskey = ? AND (%s)",
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "b.createddate", true,
                    dto.getCurrentpage(), dto.getPagesize(), Arrays.asList(dto.getPartnersyskey()));
        }

        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();

        res.setDatalist(getFormattedBatchDatalist(datalist));
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());

        res.setPagecount((int) Math.ceil((double) getTotalCount(query,
                !dto.getRole().equals("Admin") && !dto.getRole().equals("Finance")
                        ? Arrays.asList(dto.getPartnersyskey())
                        : dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()))
                / (double) dto.getPagesize()));
        res.setTotalcount(datalist.size());
        return res;
    }

    @Override
    public boolean isBatchRefCodeAvailable(String batchRefCode) throws SQLException {
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList("batchrefcode"),
                "BatchUpload where recordstatus >= 20 and batchrefcode = ?", Arrays.asList(batchRefCode));
        String batchrefcode = (String) map.get("batchrefcode");
        if (batchrefcode.isEmpty())
            return false;
        return true;
    }

    @Override
    public Map<String, Object> getBatch(long syskey)
            throws SQLException, IOException, InvalidKeyException, URISyntaxException, StorageException {

        List<String> keys = Arrays.asList("b.syskey", "b.username", "b.recordstatus", "uploadeddate", "approveddate",
                "submitteddate", "byuserid", "uploadedrecords", "fileurl", "batchrefcode", "errorfileurl",
                "errorlogurl", "voidstatus", "paymentstatus", "b.remark", "attachmenturl1", "attachmenturl2",
                "attachmenturl3", "attachmenturl3", "attachmenturl4", "attachmenturl5", "partnername", "pisyskey",
                "partnerid", "centerid", "partnersyskey", "filename", "emailaddress", "recipientsaved", "validcount",
                "invalidcount", "duration", "b.t3", "isblankcard");
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(keys,
                "BatchUpload as b left join partners as p on p.syskey = b.partnersyskey WHERE b.recordstatus <> 4 and b.syskey = ?",
                Arrays.asList(syskey));
        map.put("approvaldesc", getComboDescription(CommonConstants.APPROVAL_LIST, map, "recordstatus"));
        map.put("paymentdesc", getComboDescription(CommonConstants.PAYMENT_LIST, map, "paymentstatus"));
        map.put("voiddesc", getComboDescription(CommonConstants.VOID_LIST, map, "voidstatus"));
        map.put("errorlogurl", map.get("errorlogurl") + "?" + AzureBlobUtils.getSasToken());
        map.put("errorfileurl", map.get("errorfileurl") + "?" + AzureBlobUtils.getSasToken());
        List<Map<String, Object>> attachmentlist = new PostUploadDao().getAttachmentsByBatch(syskey);

        map.put("attachmentlist", attachmentlist.stream().map(m -> {
            // m.put("url", "https://apx.registrationsystem.org/api/downloadattachment?id="
            // + m.get("syskey"));
            try {
                m.put("url", AzureBlobUtils.getBlobClient((String) m.get("filename")).getBlobUrl() + "?"
                        + AzureBlobUtils.getSasToken());
            } catch (InvalidKeyException | URISyntaxException | StorageException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return m;
        }).collect(Collectors.toList()));
        return map;
    }

    @Override
    public PaginationResponse<Map<String, Object>> getBatchDetails(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(
                Arrays.asList("batchrefcode", "recipientsname", "gender", "fathername", "dob", "nric", "passport",
                        "nationality", "organization", "mobilephone", "division", "township", "address1", "remark"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = new ArrayList<>();
        keys.add("syskey");
        keys.add("batchrefcode");
        keys.add("recordstatus");
        keys.add("recipientid");
        keys.addAll(Arrays.asList("serialno", "recipientsname", "gender", "fathername", "dob", "nric", "passport",
                "nationality", "organization", "mobilephone", "division", "township", "address1", "remark",
                "duration"));
        keys.add("errorcolumn");
        keys.remove("fileurl");
        String query = String.format(
                "BatchDetails WHERE recordstatus <> 4 and batchuploadsyskey = ? and recordstatus %s AND (%s)",
                dto.getDetailstatus() == 2 ? "< 2" : " = ?", searchQuery);
        List<Map<String, Object>> datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "serialno",
                false, dto.getCurrentpage(), dto.getPagesize(),
                dto.getDetailstatus() == 2 ? Arrays.asList(dto.getBatchuploadsyskey())
                        : Arrays.asList(dto.getBatchuploadsyskey(), dto.getDetailstatus()));
        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();

        // res.setValidcount(String.valueOf(getTotalCount("BatchDetails WHERE
        // recordstatus = 1 and batchuploadsyskey = ?",
        // Arrays.asList(dto.getBatchuploadsyskey()))));
        // res.setInvalidcount(
        // String.valueOf(getTotalCount("BatchDetails WHERE recordstatus = 0 and
        // batchuploadsyskey = ?",
        // Arrays.asList(dto.getBatchuploadsyskey()))));
        res.setDatalist(datalist.stream().map(m -> {
            m.put("errorcolumnlist", new ArrayList<>());
            m.put("errorcolumnlistv2", new ArrayList<>());

            String errorcolumn = (String) m.get("errorcolumn");
            if (errorcolumn != null) {
                try {
                    Map<String, Object> obj = new ObjectMapper().readValue(errorcolumn, Map.class);
                    List<Map<String, String>> errorlist = (List<Map<String, String>>) obj.get("errorlist");
                    m.put("errorcolumnlist", errorlist.stream().map(m2 -> m2.get("key")).collect(Collectors.toList()));
                    m.put("errorcolumnlistv2", errorlist);
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            return m;
        }).collect(Collectors.toList()));
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());
        res.setPagecount((int) Math.ceil((double) getTotalCount(query,
                dto.getDetailstatus() == 2 ? Arrays.asList(dto.getBatchuploadsyskey())
                        : Arrays.asList(dto.getBatchuploadsyskey(), dto.getDetailstatus()))
                / (double) dto.getPagesize()));
        res.setTotalcount(datalist.size());
        return res;
    }

    @Override
    public List<Map<String, Object>> getBatchDetailsByHeader(long batchsyskey) throws SQLException {
        List<String> keys = new ArrayList<>();
        keys.add("syskey");
        keys.add("batchrefcode");
        keys.addAll(Arrays.asList("serialno", "recipientsname", "gender", "fathername", "dob", "nric", "passport",
                "nationality", "organization", "mobilephone", "division", "township", "address1", "remark"));
        return new EasySql(DbFactory.getConnection()).getMany(keys, "BatchDetails where batchuploadsyskey = ?",
                Arrays.asList(batchsyskey));
    }

    public List<Map<String, Object>> getBatchDetailsByHeader(long batchsyskey, int status) throws SQLException {
        List<String> keys = new ArrayList<>();
        keys.add("syskey");
        keys.add("batchrefcode");
        keys.addAll(Arrays.asList("serialno", "recipientsname", "gender", "fathername", "dob", "nric", "passport",
                "nationality", "organization", "mobilephone", "division", "township", "address1", "remark"));
        keys.add("partnersyskey");
        return new EasySql(DbFactory.getConnection()).getMany(keys,
                "BatchDetails where batchuploadsyskey = ? and recordstatus = ? order by serialno",
                Arrays.asList(batchsyskey, status));
    }

    public int updateBatchDetail(String idfield, Map<String, Object> args) throws SQLException {
        return new EasySql(DbFactory.getConnection()).updateOne("BatchDetails", idfield, args);

    }

    public int updateBatchDetail(String serialno, long syskey, Map<String, Object> args) throws SQLException {
        List<String> keys = args.entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        final String sql = String.format("update BatchDetails set %s where serialno = ? and batchuploadsyskey = ?",
                String.join(", ", keys.stream().map(k -> k + " = ?").collect(Collectors.toList())));

        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 1;
            for (String key : keys) {
                stmt.setObject(i++, args.get(key));
            }
            stmt.setObject(i++, serialno);
            stmt.setObject(i++, syskey);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int updateBatchDetail(Map<String, Object> args) throws SQLException {
        return new EasySql(DbFactory.getConnection()).updateOne("BatchDetails", "syskey", args);
    }

    @Override
    public int updateBatch(Map<String, Object> args) throws SQLException {
        return new EasySql(DbFactory.getConnection()).updateOne("BatchUpload", "syskey", args);
    }

    @Override
    public boolean isBatchNoValid(String filename) throws SQLException {
        List<Map<String, Object>> datalist = getDBClient().getMany(Arrays.asList("batchrefcode"),
                "batchupload where batchrefcode = ?", Arrays.asList(getBatchRefCode(filename)));
        if (datalist.size() > 0) {
            return true;
        }
        final String sql = String.format(
                "select MAX(SUBSTRING(batchrefcode, 11, 3)) as no from BatchUpload where batchrefcode like %s",
                "'" + filename.split("-")[1] + "%'");
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int newno = Integer.parseInt(filename.split("-")[2].substring(1));
            if (newno == 0) {
                return false;
            }
            while (rs.next()) {
                int oldno = rs.getInt("no");
                if (oldno + 1 != newno) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isBatchPaymentValid(String batchsyskey, String pisyskey, String partnersyskey) throws SQLException {
        int total1 = getTotalCount("BatchUpload where recordstatus <> 4 and syskey = ? and partnersyskey = ?",
                Arrays.asList(batchsyskey, partnersyskey));
        int total2 = getTotalCount("ProformaInvoice where recordstatus <> 4 and syskey = ? and partnersyskey = ?",
                Arrays.asList(pisyskey, partnersyskey));
        if (total1 > 0 && total2 > 0) {
            return true;
        }
        return false;
    }

    public boolean isOwnBatch(String batchsyskey, String partnersyskey) throws SQLException {
        int total = getTotalCount("BatchUpload where recordstatus <> 4 and syskey = ? and partnersyskey = ?",
                Arrays.asList(batchsyskey, partnersyskey));
        if (total > 0) {
            return true;
        }
        return false;
    }
}
