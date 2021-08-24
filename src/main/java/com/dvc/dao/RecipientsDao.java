package com.dvc.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.dvc.factory.DbFactory;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.RecipientsDto;
import com.dvc.models.ReportDto;
import com.dvc.utils.Cid;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;

public class RecipientsDao extends BaseDao implements IRecipientsDao {

    private String getFilterQuery(FilterDto dto) {
        if (!dto.getPartnersyskey().isEmpty() && dto.getCenterid().isEmpty()) {
            return "and partnersyskey = ?";
        } else if (dto.getPartnersyskey().isEmpty() && !dto.getCenterid().isEmpty()) {
            return "and cid like " + "'" + dto.getCenterid() + "%'";
        } else if (!dto.getPartnersyskey().isEmpty() && !dto.getCenterid().isEmpty()) {
            return "and cid like " + "'" + dto.getCenterid() + "%' and partnersyskey = ?";
        }
        return "";
    }

    private String getDoseCondition(FilterDto dto) {
        if (dto.isAlldose()) {
            return "";
        }
        String condition = "and dose ";
        if (dto.getOperator() == 0) {
            condition += "= " + String.valueOf(dto.getDosecount());
        } else if (dto.getOperator() == 1) {
            condition += "> " + String.valueOf(dto.getDosecount());
        }
        return condition;
    }

    @Override
    public PaginationResponse<Map<String, Object>> getRecipients(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("cid", "recipientsname", "nric", "township"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("r.syskey", "rid", "r.cid", "recipientsname", "fathername", "dob", "age",
                "nric", "passport", "nationality", "organization", "township", "division", "mobilephone",
                "registerationstatus", "vaccinationstatus", "batchrefcode", "partnername", "partnerid",
                "r.partnersyskey", "voidstatus", "dose");
        if (!(dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0))) {
            keys = Arrays.asList("r.syskey", "rid", "r.cid", "recipientsname", "fathername", "dob", "age", "nric",
                    "passport", "nationality", "organization", "township", "division", "mobilephone",
                    "registerationstatus", "vaccinationstatus", "batchrefcode", "partnername", "partnerid",
                    "r.partnersyskey", "voidstatus", "dose", "doseupdatetime", "lot", "doctor", "d.remark", "d.userid");
        }

        String query = "";
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getRole().equals("Admin") || dto.getRole().equals("Finance")) {

            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey %s WHERE r.recordstatus <> 4 %s %s %s %s and (%s)",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0) ? ""
                            : "left join DoseRecords as d on r.cid = d.cid",
                    getFilterQuery(dto), getDoseCondition(dto),
                    !dto.getVoidstatus().isEmpty() ? "and voidstatus = " + dto.getVoidstatus() : "",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0)
                            || dto.getDoseupdatedate().isEmpty() ? ""
                                    : String.format("and DATEDIFF(day, doseupdatetime, '%s') = 0",
                                            dto.getDoseupdatedate()),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "cid", true, dto.getCurrentpage(),
                    dto.getPagesize(),
                    dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        } else {
            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey %s WHERE r.recordstatus <> 4 AND r.partnersyskey = ? %s %s %s %s and (%s)",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0) ? ""
                            : "left join DoseRecords as d on r.cid = d.cid",
                    !dto.getCenterid().isEmpty() ? "and cid like " + "'" + dto.getCenterid() + "%'" : "",
                    getDoseCondition(dto),
                    !dto.getVoidstatus().isEmpty() ? "and voidstatus = " + dto.getVoidstatus() : "",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0)
                            || dto.getDoseupdatedate().isEmpty() ? ""
                                    : String.format("and DATEDIFF(day, doseupdatetime, '%s') = 0",
                                            dto.getDoseupdatedate()),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "cid", true, dto.getCurrentpage(),
                    dto.getPagesize(), Arrays.asList(dto.getPartnersyskey()));
        }

        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        int totalcount = getTotalCount(query,
                !dto.getRole().equals("Admin") && !dto.getRole().equals("Finance")
                        ? Arrays.asList(dto.getPartnersyskey())
                        : dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        res.setDatalist(datalist);
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());
        res.setPagecount((int) Math.ceil((double) totalcount / (double) dto.getPagesize()));
        res.setTotalcount(totalcount);
        return res;
    }

    @Override
    public Map<String, Object> getRecipient(long syskey) throws SQLException {
        return getRecipientByKey("r.syskey", syskey);
    }

    @Override
    public int updateRecipient(RecipientsDto dto) throws SQLException, IOException {
        EasyData<RecipientsDto> easyData = new EasyData<>(dto);
        Map<String, Object> args = easyData.toMap();
        args.put("modifieddate", Instant.now().toString());
        String dob = dto.getDob();
        int dobyear = Integer.parseInt(dob.substring(0, 4));
        int year = LocalDate.now().getYear();
        args.put("age", year - dobyear);
        return new EasySql(DbFactory.getConnection()).updateOne("Recipients", "syskey", args);
    }

    public int updateRecipient(Map<String, Object> args) throws SQLException {
        return getDBClient().updateOne("Recipients", "syskey", args);
    }

    public String getCidRange(long batchsyskey) throws SQLException {
        final String sql = "select MAX(cid) as max, MIN(cid) as min from Recipients where batchuploadsyskey = ?";
        String range = "";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, batchsyskey);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("min") != null && rs.getString("max") != null) {
                    range = rs.getString("min") + "-" + rs.getString("max");
                }
            }
        }
        return range;
    }

    @Override
    public int deleteRecipient(RecipientsDto dto) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", dto.getSyskey());
        args.put("recordstatus", 4);
        args.put("modifieddate", Instant.now().toString());
        return new EasySql(DbFactory.getConnection()).updateOne("Recipients", "syskey", args);
    }

    @Override
    public long addRecipient(RecipientsDto dto) throws SQLException, IOException {
        final long syskey = KeyGenerator.generateSyskey();
        final String now = Instant.now().toString();
        EasyData<RecipientsDto> easyData = new EasyData<>(dto);
        Map<String, Object> args = easyData.toMap();
        args.put("syskey", syskey);
        args.put("createddate", now);
        args.put("modifieddate", now);
        args.put("rid", KeyGenerator.generateID());

        String dob = dto.getDob();
        int dobyear = Integer.parseInt(dob.substring(0, 4));
        int year = LocalDate.now().getYear();
        args.put("age", year - dobyear);
        args.put("PIRef", ""); // recheck
        args.put("registerationstatus", 10);
        args.put("voidstatus", 1);
        int[] result = new EasySql(DbFactory.getConnection()).insertMany("Recipients", Arrays.asList(args));
        if (result.length == 0 || result[0] == 0) {
            return 0;
        }
        return syskey;
    }

    @Override
    public int changeRegistrationsStatus(long batchSyskey, int status) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("batchuploadsysKey", batchSyskey);
        args.put("registerationstatus", status);
        return new EasySql(DbFactory.getConnection()).updateOne("Recipients", "batchuploadsysKey", args);
    }

    @Override
    public boolean isAvailable(String fieldname, Object value) throws SQLException {
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList(fieldname),
                String.format("Recipients where recordstatus <> 4 and %s = ?", fieldname), Arrays.asList(value));
        String result = (String) map.get(fieldname);
        if (result.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean isAvailable(String fieldname, Object value, String fieldname2, Object value2) throws SQLException {
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(Arrays.asList(fieldname),
                String.format("Recipients where recordstatus <> 4 and %s = ? and %s = ?", fieldname, fieldname2),
                Arrays.asList(value, value2));
        String result = (String) map.get(fieldname);
        if (result.isEmpty()) {
            return false;
        }
        return true;
    }

    public Map<String, Object> getRecipientByKey(String key, Object value) throws SQLException {
        List<String> keys = Arrays.asList("r.syskey", "r.remark", "rid", "cid", "recipientsname", "fathername",
                "gender", "dob", "age", "nric", "passport", "nationality", "organization", "address1", "township",
                "division", "mobilephone", "registerationstatus", "vaccinationstatus", "qrtoken", "partnerid",
                "voidstatus", "partnername", "r.dose", "r.t10");
        String query = String.format(
                "Recipients as r left join Partners as p on r.partnersyskey = p.syskey WHERE r.recordstatus <> 4 AND %s = ?",
                key);

        return new EasySql(DbFactory.getConnection()).getOne(keys, query, Arrays.asList(value));
    }

    public List<Map<String, Object>> getRecipientsByKey(String key, Object value) throws SQLException {
        List<String> keys = Arrays.asList("syskey", "remark", "rid", "cid", "recipientsname", "fathername", "gender",
                "dob", "age", "nric", "passport", "nationality", "organization", "address1", "township", "division",
                "mobilephone", "registerationstatus", "vaccinationstatus", "qrtoken");
        String query = String.format("Recipients WHERE recordstatus <> 4 AND %s = ?", key);

        return new EasySql(DbFactory.getConnection()).getMany(keys, query, Arrays.asList(value));
    }

    public int[] saveRecipientsFromPI(List<Map<String, Object>> datalist) throws SQLException {
        return new EasySql(DbFactory.getConnection()).insertMany("Recipients", datalist);
    }

    @Override
    public List<Map<String, Object>> getRegisteredRecipients(long pisyskey) throws SQLException {
        List<String> keys = Arrays.asList("syskey", "remark", "rid", "cid", "recipientsname", "fathername", "gender",
                "dob", "age", "nric", "passport", "nationality", "organization", "address1", "township", "division",
                "mobilephone", "registerationstatus", "vaccinationstatus", "qrtoken");
        return getDBClient().getMany(keys, "Recipients where pisyskey = ? and batchuploadsyskey = 0",
                Arrays.asList(pisyskey));

    }

    @Override
    public int voidRecipient(long syskey, int status) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", syskey);
        args.put("voidstatus", status);
        args.put("modifieddate", Instant.now().toString());
        int voidcount = getDBClient().updateOne("Recipients", "syskey", args);

        args = getDBClient().getOne(Arrays.asList("p.syskey", "voidcount"),
                "Recipients as r left join ProformaInvoice as p on r.pisyskey = p.syskey where r.syskey = ?",
                Arrays.asList(syskey));
        if (args.get("syskey") == null) {
            return 0;
        }
        if (status == 0) {
            args.put("voidcount", Integer.parseInt((String) args.get("voidcount")) + voidcount);
        } else {
            args.put("voidcount", Integer.parseInt((String) args.get("voidcount")) - voidcount);
        }
        return getDBClient().updateOne("ProformaInvoice", "syskey", args);
    }

    @Override
    public boolean isOwnRecipient(String syskey, String partnersyskey) throws SQLException {
        int total = getTotalCount("Recipients where syskey = ? and partnersyskey = ?",
                Arrays.asList(syskey, partnersyskey));
        if (total > 0) {
            return true;
        }
        return false;
    }

    public int voidRecipientsByBatch(String batchsyskey, int voidstatus) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("batchuploadsyskey", batchsyskey);
        args.put("voidstatus", voidstatus);
        // int voidcount = getDBClient().updateOne("Recipients", "batchuploadsyskey",
        // args);
        if (voidstatus == 0) {
            final String sql = "update Recipients set voidstatus = ? where batchuploadsyskey = ? and voidstatus = 1";
            try (Connection connection = DbFactory.getConnection();
                    PreparedStatement stmt = connection.prepareStatement(sql)) {
                int i = 1;
                stmt.setInt(i++, voidstatus);
                stmt.setString(i++, batchsyskey);
                int voidcount = stmt.executeUpdate();
                Map<String, Object> batch = getDBClient().getOne(Arrays.asList("pisyskey"),
                        "BatchUpload where syskey = ?", Arrays.asList(batchsyskey));
                if (batch.get("pisyskey") == null) {
                    return 0;
                }
                args = getDBClient().getOne(Arrays.asList("syskey", "voidcount"), "ProformaInvoice where syskey = ?",
                        Arrays.asList(batch.get("pisyskey")));
                args.put("voidcount", Integer.parseInt((String) args.get("voidcount")) + voidcount);
                return getDBClient().updateOne("ProformaInvoice", "syskey", args);
            }
        } else {
            final String sql = "update Recipients set voidstatus = ? where batchuploadsyskey = ? and voidstatus = 0";
            try (Connection connection = DbFactory.getConnection();
                    PreparedStatement stmt = connection.prepareStatement(sql)) {
                int i = 1;
                stmt.setInt(i++, voidstatus);
                stmt.setString(i++, batchsyskey);
                int voidcount = stmt.executeUpdate();
                Map<String, Object> batch = getDBClient().getOne(Arrays.asList("pisyskey"),
                        "BatchUpload where syskey = ?", Arrays.asList(batchsyskey));
                if (batch.get("pisyskey") == null) {
                    return 0;
                }
                args = getDBClient().getOne(Arrays.asList("syskey", "voidcount"), "ProformaInvoice where syskey = ?",
                        Arrays.asList(batch.get("pisyskey")));
                args.put("voidcount", Integer.parseInt((String) args.get("voidcount")) - voidcount);
                return getDBClient().updateOne("ProformaInvoice", "syskey", args);
            }
        }

    }

    public int[] addDoseInfo(Map<String, Object> info) throws SQLException {
        final String now = Instant.now().toString();
        info.put("syskey", KeyGenerator.generateSyskey());
        info.put("createddate", now);
        info.put("modifieddate", now);
        info.put("doseupdatetime", now);
        return getDBClient().insertMany("DoseRecords", Arrays.asList(info));
    }

    public List<Map<String, Object>> getYgn1Latest() throws SQLException {
        return getDBClient().getMany(
                Arrays.asList("cl.cid", "firstdosedate", "firstdosetime", "seconddosetime", "centername"),
                "CenterLastSerials as cl left join Recipients as r on r.cid = cl.cid left join Centers as c on c.centerid = cl.centerid where cl.centerid = 'YGN1'");
    }

    public List<Map<String, Object>> getSummary() throws SQLException {

        List<Map<String, Object>> datalist = getDBClient().getMany(
                Arrays.asList("cl.cid", "c.centerid", "firstdosedate", "firstdosetime", "seconddosetime", "centername"),
                "CenterLastSerials as cl left join Recipients as r on r.cid = cl.cid left join Centers as c on c.centerid = cl.centerid");

        for (Map<String, Object> data : datalist) {
            // data.put("cards", getTotalCount("Recipients where cid like '" +
            // data.get("centerid") + "%'"));
            // data.put("cards", Integer.parseInt(((String)
            // data.get("cid")).replaceAll("^([a-zA-Z]{1,3}[0-9])", "")));
            data.put("cards", Cid.getNumberFromCid((String) data.get("cid")));
            // data.put("picount",
            // getTotalCount("ProformaInvoice where centerid = ?",
            // Arrays.asList(data.get("centerid"))));
            // data.put("batchcount", getTotalCount(
            // "BatchUpload as b left join ProformaInvoice as pi on b.pisyskey = pi.syskey
            // where pi.centerid = ?",
            // Arrays.asList(data.get("centerid"))));
            // data.put("partnercount", getTotalCount(
            // "Partners as p left join ProformaInvoice as pi on p.syskey = pi.partnersyskey
            // where pi.centerid = ?",
            // Arrays.asList(data.get("centerid"))));

        }
        return datalist;
    }

    public List<LinkedHashMap<String, Object>> getRecipients(ReportDto dto) throws SQLException {
        String condition = "";
        if (dto.getOperator() == 0) {
            condition += "=";
        } else if (dto.getOperator() == 1) {
            condition += ">";
        } else if (dto.getOperator() == -1) {
            condition += "<";
        }

        List<String> keys = Arrays.asList("r.cid", "doseupdatetime", "lot", "doctor", "d.remark", "r.dose",
                "recipientsname", "gender", "fathername", "dob", "nric", "age", "passport", "organization",
                "nationality", "division", "township", "mobilephone", "address1");
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getRole().equals("Partner")) {
            datalist = getDBClient().getMany(keys, String.format(
                    "Recipients as r left join DoseRecords as d on r.cid = d.cid where partnersyskey = ? and dose %s ?",
                    condition), Arrays.asList(dto.getPartnersyskey(), dto.getDosecount()));
        } else {
            datalist = getDBClient().getMany(keys, String
                    .format("Recipients as r left join DoseRecords as d on r.cid = d.cid where dose %s ?", condition),
                    Arrays.asList(dto.getDosecount()));
        }
        List<LinkedHashMap<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : datalist) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("CID", m.get("cid"));
            data.put("Name", m.get("recipientsname"));
            data.put("Sex", m.get("gender"));
            data.put("Father's Name", m.get("fathername"));
            data.put("DOB", m.get("dob"));
            data.put("NRC", m.get("nric"));
            data.put("Passport", m.get("passport"));
            data.put("Organization", m.get("organization"));
            data.put("Mobile", m.get("mobilephone"));
            data.put("State/Region", m.get("division"));
            data.put("Address", m.get("address1"));
            data.put("Dose", m.get("dose"));
            data.put("Lot No.", m.get("lot"));
            data.put("Doctor/Nurse", m.get("doctor"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            // Asia/Rangoon
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Rangoon"));
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsed = parser.parse((String) m.get("doseupdatetime"));

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                data.put("1st Dose Date", formatter.format(parsed));
                data.put("2nd Dose Date", "");
            } catch (ParseException e) {

                e.printStackTrace();
            }
            data.put("Township", m.get("township"));
            data.put("Remark", m.get("remark"));
        }
        datalist.stream().map(m -> {
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("CID", m.get("cid"));
            data.put("Name", m.get("recipientsname"));
            data.put("Sex", m.get("gender"));
            data.put("Father's Name", m.get("fathername"));
            data.put("DOB", m.get("dob"));
            data.put("NRC", m.get("nric"));
            data.put("Passport", m.get("passport"));
            data.put("Organization", m.get("organization"));
            data.put("Mobile", m.get("mobilephone"));
            data.put("State/Region", m.get("division"));
            data.put("Address", m.get("address1"));
            data.put("Dose", m.get("dose"));
            data.put("Lot No.", m.get("lot"));
            data.put("Doctor/Nurse", m.get("doctor"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            // Asia/Rangoon
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Rangoon"));
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsed = parser.parse((String) m.get("doseupdatetime"));

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                data.put("1st Dose Date", formatter.format(parsed));
                data.put("2nd Dose Date", "");
            } catch (ParseException e) {

                e.printStackTrace();
            }
            data.put("Township", m.get("township"));
            data.put("Remark", m.get("remark"));
            return data;
        }).collect(Collectors.toList());

        return list;

    }
}
