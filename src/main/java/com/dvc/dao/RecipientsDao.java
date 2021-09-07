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
import com.dvc.models.UpdateRecipientDto;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RecipientsDao extends BaseDao implements IRecipientsDao {

    private String getFilterQuery(FilterDto dto) {
        if (!dto.getPartnersyskey().isEmpty() && dto.getCenterid().isEmpty()) {
            return "and r.partnersyskey = ?";
        } else if (dto.getPartnersyskey().isEmpty() && !dto.getCenterid().isEmpty()) {
            return "and r.centerid = '" + dto.getCenterid() + "'";
            // return "and r.cid like " + "'" + dto.getCenterid() + "%'";
            // return "and SUBSTRING(r.cid, 1, LEN(r.cid) - 7) = '" + dto.getCenterid() +
            // "'";
        } else if (!dto.getPartnersyskey().isEmpty() && !dto.getCenterid().isEmpty()) {
            return "and r.centerid = " + "'" + dto.getCenterid() + "' and r.partnersyskey = ?";
            // return "and SUBSTRING(r.cid, 1, LEN(r.cid) - 7) = '" + dto.getCenterid() + "'
            // and partnersyskey = ?";
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
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("r.cid", "recipientsname", "nric", "township"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        // if (dto.getCenterid().equals("YGN")) {
        // dto.setCenterid("YGN0");
        // }
        List<String> keys = Arrays.asList("r.syskey", "rid", "r.cid", "recipientsname", "fathername", "dob", "age",
                "nric", "passport", "nationality", "organization", "township", "division", "mobilephone",
                "registerationstatus", "vaccinationstatus", "batchrefcode", "partnername", "partnerid",
                "r.partnersyskey", "voidstatus", "dose", "gender", "address1");
        if (!(dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0))) {
            keys = Arrays.asList("r.syskey", "rid", "r.cid", "recipientsname", "fathername", "dob", "age", "nric",
                    "passport", "nationality", "organization", "township", "division", "mobilephone",
                    "registerationstatus", "vaccinationstatus", "batchrefcode", "partnername", "partnerid",
                    "r.partnersyskey", "voidstatus", "dose", "gender", "address1", "doseupdatetime", "lot", "doctor",
                    "d.remark", "d.userid");
        }

        String query = "";
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (!dto.getRole().equals("Partner")) {

            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey left join ProformaInvoice as pi on r.pisyskey = pi.syskey %s WHERE r.recordstatus <> 4 %s %s %s %s and (%s)",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0) ? ""
                            : "left join DoseRecords as d on r.cid = d.cid",
                    getFilterQuery(dto), getDoseCondition(dto),
                    !dto.getVoidstatus().isEmpty() ? "and voidstatus = " + dto.getVoidstatus() : "",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0)
                            || dto.getDoseupdatedate().isEmpty() ? ""
                                    : String.format("and DATEDIFF(day, doseupdatetime, '%s') = 0",
                                            dto.getDoseupdatedate()),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "cid", dto.isReverse(),
                    dto.getCurrentpage(), dto.getPagesize(),
                    dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        } else {
            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey left join ProformaInvoice as pi on r.pisyskey = pi.syskey %s WHERE r.recordstatus <> 4 AND r.partnersyskey = ? %s %s %s %s and (%s)",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0) ? ""
                            : "left join DoseRecords as d on r.cid = d.cid",
                    !dto.getCenterid().isEmpty() ? "and r.centerid = '" + dto.getCenterid() + "'" : "",
                    getDoseCondition(dto),
                    !dto.getVoidstatus().isEmpty() ? "and voidstatus = " + dto.getVoidstatus() : "",
                    dto.isAlldose() || (dto.getOperator() == 0 && dto.getDosecount() == 0)
                            || dto.getDoseupdatedate().isEmpty() ? ""
                                    : String.format("and DATEDIFF(day, doseupdatetime, '%s') = 0",
                                            dto.getDoseupdatedate()),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "cid", dto.isReverse(),
                    dto.getCurrentpage(), dto.getPagesize(), Arrays.asList(dto.getPartnersyskey()));
        }

        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        int totalcount = getTotalCount(query, dto.getRole().equals("Partner") ? Arrays.asList(dto.getPartnersyskey())
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
    public int updateRecipient(UpdateRecipientDto dto) throws SQLException, IOException {
        EasyData<UpdateRecipientDto> easyData = new EasyData<>(dto);
        Map<String, Object> args = easyData.toMapExcept(Arrays.asList("userid", "username"));
        args.put("modifieddate", Instant.now().toString());
        args.put("age", dto.getAge());
        return new EasySql(DbFactory.getConnection()).updateOne("Recipients", "cid", args);
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
                "voidstatus", "partnername", "r.dose", "r.t10", "firstdosedate", "firstdosetime", "seconddosetime",
                "ward", "occupation");
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

    public boolean isOwnRecipientV2(String cid, String partnersyskey) throws SQLException {
        int total = getTotalCount("Recipients where cid = ? and partnersyskey = ?", Arrays.asList(cid, partnersyskey));
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

    public List<Map<String, Object>> getSummary(String role, String partnersyskey) throws SQLException {
        final String sql = "select s1.centerid, s1.centername, s1.cards, s1.cid, s1.doses, s2.voidcount from (select r.centerid, c.centername, count(r.cid) as cards, max(cid) as cid, sum(dose) as doses from [dbo].[Recipients] as r left join [dbo].[Centers] as c on c.centerid = r.centerid group by r.centerid, c.centername) as s1 left join (select centerid, count(cid) as voidcount from [dbo].[Recipients] where voidstatus = 0 group by centerid) as s2 on s1.centerid = s2.centerid";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("centerid", rs.getString("centerid"));
                data.put("centername", rs.getString("centername"));
                data.put("cards", rs.getString("cards"));
                data.put("doses", rs.getString("doses"));
                data.put("cid", rs.getString("cid"));
                data.put("voidcount", rs.getString("voidcount") == null ? "0" : rs.getString("voidcount"));
                data.put("firstdosedate", "");
                data.put("firstdosetime", "");
                data.put("seconddosetime", "");
                if (rs.getString("centerid").equals("YGN1") || rs.getString("centerid").equals("YGN")) {
                    List<Map<String, Object>> list = getDBClient().getMany(
                            Arrays.asList("firstdosedate", "firstdosetime", "seconddosetime"),
                            "Recipients where cid = ?", Arrays.asList(data.get("cid")));
                    if (list.size() > 0) {
                        data.putAll(list.get(0));
                    }
                }
                datalist.add(data);
            }
            return datalist;
        }
    }

    // public List<Map<String, Object>> getSummary(String role, String
    // partnersyskey) throws SQLException {
    // List<String> keys = Arrays.asList("cl.cid", "c.centerid", "firstdosedate",
    // "firstdosetime", "seconddosetime",
    // "centername");
    // List<Map<String, Object>> datalist = new ArrayList<>();
    // if (role.equals("Partner")) {
    // datalist = getDBClient().getMany(keys,
    // "CenterLastSerials as cl left join Recipients as r on r.cid = cl.cid left
    // join Centers as c on c.centerid = cl.centerid where cl.centerid in (select
    // centerid from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi
    // on r.pisyskey = pi.syskey where r.partnersyskey = ? group by pi.centerid)",
    // Arrays.asList(partnersyskey));
    // } else {
    // datalist = getDBClient().getMany(keys,
    // "CenterLastSerials as cl left join Recipients as r on r.cid = cl.cid left
    // join Centers as c on c.centerid = cl.centerid");
    // }

    // for (Map<String, Object> data : datalist) {
    // // data.put("cards", getTotalCount("Recipients where cid like '" +
    // // data.get("centerid") + "%'"));
    // // data.put("cards", Integer.parseInt(((String)
    // // data.get("cid")).replaceAll("^([a-zA-Z]{1,3}[0-9])", "")));
    // data.put("cards", Cid.getNumberFromCid((String) data.get("cid")));
    // // data.put("voidcount",
    // // getTotalCount("Recipients where voidstatus = 0 and SUBSTRING(cid, 1,
    // LEN(cid)
    // // - 7) = ?",
    // // Arrays.asList(data.get("centerid"))));
    // // data.put("picount",
    // // getTotalCount("ProformaInvoice where centerid = ?",
    // // Arrays.asList(data.get("centerid"))));
    // // data.put("batchcount", getTotalCount(
    // // "BatchUpload as b left join ProformaInvoice as pi on b.pisyskey =
    // pi.syskey
    // // where pi.centerid = ?",
    // // Arrays.asList(data.get("centerid"))));
    // // data.put("partnercount", getTotalCount(
    // // "Partners as p left join ProformaInvoice as pi on p.syskey =
    // pi.partnersyskey
    // // where pi.centerid = ?",
    // // Arrays.asList(data.get("centerid"))));

    // }
    // return datalist;
    // }

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

    public boolean isDoseUpdated(String cid) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String date = formatter.format(LocalDate.now());
        int total = getTotalCount("DoseRecords where cid = ? and DATEDIFF(day, doseupdatetime, ?) = 0",
                Arrays.asList(cid, date));
        if (total > 0) {
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> getSubmittedRecipient(String cid) throws SQLException, IOException {
        List<String> keys = Arrays.asList("syskey", "cid", "nric", "passport", "gender", "dob", "age", "recipientsname",
                "mobilephone", "address1", "firstdosedate", "firstdosetime", "seconddosetime", "userid", "username");
        return getDBClient().getMany(keys, "SubmittedRecipients where cid = ? and recordstatus <> 30",
                Arrays.asList(cid));
    }

    public int submitRecipient(UpdateRecipientDto dto) throws SQLException, IOException {
        final String now = Instant.now().toString();
        Map<String, Object> args = new EasyData<UpdateRecipientDto>(dto).toMap();
        Map<String, Object> oldRecipient = getDBClient().getOne(Arrays.asList("nric", "passport", "recipientsname"),
                "Recipients where cid = ?", Arrays.asList(dto.getCid()));

        if (!oldRecipient.get("nric").equals(dto.getNric())
                || !oldRecipient.get("passport").equals(dto.getPassport())) {
            args.put("recipientsname", oldRecipient.get("recipientsname"));
        } else if (!oldRecipient.get("recipientsname").equals(dto.getRecipientsname())) {
            args.put("passport", oldRecipient.get("passport"));
            args.put("nric", oldRecipient.get("nric"));
        }

        args.put("modifieddate", now);
        args.put("age", dto.getAge());
        List<Map<String, Object>> datalist = getDBClient().getMany(Arrays.asList("syskey"),
                "SubmittedRecipients where cid = ? and recordstatus <> 30", Arrays.asList(dto.getCid()));
        if (datalist.size() > 0) {
            args.put("syskey", datalist.get(0).get("syskey"));
            args.remove("cid");
            return getDBClient().updateOne("SubmittedRecipients", "syskey", args);
        }

        args.put("syskey", KeyGenerator.generateSyskey());
        args.put("createddate", now);
        getDBClient().insertMany("SubmittedRecipients", Arrays.asList(args));
        return 1;
    }

    private Map<String, Object> getCard(String cid) throws SQLException {
        return getDBClient().getOne(
                Arrays.asList("cid", "recipientsname", "fathername", "gender", "nric", "passport", "nationality", "dob",
                        "age", "organization", "occupation", "address1", "township", "division", "mobilephone", "piref",
                        "batchuploadsyskey", "partnersyskey", "pisyskey", "voidstatus", "qrtoken", "batchrefcode",
                        "dose", "firstdosedate", "firstdosetime", "seconddosetime", "centerid"),
                "Recipients where cid = ?", Arrays.asList(cid));
    }

    public int approveRecipientV2(UpdateRecipientDto dto) throws SQLException, IOException {

        final String now = Instant.now().toString();

        Map<String, Object> oldRecipient = getCard(dto.getCid());
        long refno = KeyGenerator.generateSyskey();
        oldRecipient.put("syskey", KeyGenerator.generateSyskey());
        oldRecipient.put("userid", dto.getUserid());
        oldRecipient.put("username", dto.getUsername());
        oldRecipient.put("createddate", now);
        oldRecipient.put("modifieddate", now);
        oldRecipient.put("refno", refno);
        oldRecipient.put("pairkey", 1);

        Map<String, Object> args = new EasyData<UpdateRecipientDto>(dto).toMap();
        if (oldRecipient.get("nric") == null) {
            oldRecipient.put("nric", "");
        }
        if (oldRecipient.get("passport") == null) {
            oldRecipient.put("passport", "");
        }
        if (oldRecipient.get("recipientsname") == null) {
            oldRecipient.put("recipientsname", "");
        }
        if (!oldRecipient.get("nric").equals(dto.getNric())
                || !oldRecipient.get("passport").equals(dto.getPassport())) {
            args.put("recipientsname", oldRecipient.get("recipientsname"));
        } else if (!oldRecipient.get("recipientsname").equals(dto.getRecipientsname())) {
            args.put("passport", oldRecipient.get("passport"));
            args.put("nric", oldRecipient.get("nric"));
        }

        getDBClient().updateOne("Recipients", "cid", args);
        Map<String, Object> recipient = getCard(dto.getCid());
        recipient.put("syskey", KeyGenerator.generateSyskey());
        recipient.put("userid", dto.getUserid());
        recipient.put("username", dto.getUsername());
        recipient.put("createddate", now);
        recipient.put("modifieddate", now);
        recipient.put("refno", refno);
        recipient.put("pairkey", 2);

        getDBClient().insertMany("RecipientsHistory", Arrays.asList(oldRecipient, recipient));
        return 1;
    }

    public int approveRecipient(String cid, String userid, String username) throws SQLException, IOException {
        List<Map<String, Object>> datalist = getSubmittedRecipient(cid);
        if (datalist.size() == 0) {
            return 0;
        }
        final String now = Instant.now().toString();
        Map<String, Object> args = datalist.get(0);
        String submittedsyskey = (String) args.get("syskey");
        args.remove("syskey");
        Map<String, Object> oldRecipient = getCard(cid);
        long refno = KeyGenerator.generateSyskey();
        oldRecipient.put("syskey", KeyGenerator.generateSyskey());
        oldRecipient.put("userid", userid);
        oldRecipient.put("username", username);
        oldRecipient.put("createddate", now);
        oldRecipient.put("modifieddate", now);
        oldRecipient.put("refno", refno);
        oldRecipient.put("pairkey", 1);

        getDBClient().updateOne("Recipients", "cid", args);
        Map<String, Object> recipient = getCard(cid);
        recipient.put("syskey", KeyGenerator.generateSyskey());
        recipient.put("userid", userid);
        recipient.put("username", username);
        recipient.put("createddate", now);
        recipient.put("modifieddate", now);
        recipient.put("refno", refno);
        recipient.put("pairkey", 2);

        getDBClient().insertMany("RecipientsHistory", Arrays.asList(oldRecipient, recipient));
        Map<String, Object> data = new HashMap<>();
        data.put("syskey", submittedsyskey);
        data.put("modifieddate", now);
        data.put("recordstatus", 30);
        return getDBClient().updateOne("SubmittedRecipients", "syskey", data);
    }

    public PaginationResponse<Map<String, Object>> getMohsRecipients(FilterDto dto) throws SQLException {
        String sql = "select r.syskey, r.cid, rid, recipientsname, fathername, dob, age, nric, passport, nationality, organization, township, division, mobilephone, batchrefcode, partnername, partnerid, r.partnersyskey, voidstatus, dose, gender, address1, prefixnrc, nrccode, nrctype, nrcno, ward, street, occupation, isexported, vaccinationcenter from ";
        String query = "Recipients as r left join Partners as p on p.syskey = r.partnersyskey where voidstatus = 1";

        List<Object> args = new ArrayList<>();
        if (dto.getRole().equals("Partner") || (dto.getRole().equals("Admin") && !dto.getPartnersyskey().isEmpty())) {
            query += " and r.partnersyskey = ? ";
            args.add(dto.getPartnersyskey());
        }
        query += getDoseCondition(dto);
        if (!dto.getBatchuploadsyskey().isEmpty()) {
            query += " and batchuploadsyskey = ? ";
            args.add(dto.getBatchuploadsyskey());
        }
        if (!dto.getCenterid().isEmpty() && !dto.getStartcid().isEmpty() && !dto.getEndcid().isEmpty()) {
            query += " and r.centerid = ? and r.cid between ? and ? ";
            args.add(dto.getCenterid());
            args.add(dto.getStartcid());
            args.add(dto.getEndcid());
        }

        sql += query + " ORDER BY r.cid desc OFFSET ? ROWS FETCH FIRST ? ROWS ONLY";

        List<Map<String, Object>> datalist = new ArrayList<>();

        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            for (Object value : args) {
                stmt.setObject(i++, value);
            }
            int offset = (dto.getCurrentpage() - 1) * dto.getPagesize();
            stmt.setObject(i++, offset);
            stmt.setObject(i++, dto.getPagesize());

            ResultSet rs = stmt.executeQuery();
            List<String> cidlist = new ArrayList<>();
            while (rs.next()) {
                cidlist.add("'" + rs.getString("cid") + "'");
                Map<String, Object> map = new HashMap<>();
                map.put("syskey", rs.getString("syskey"));
                map.put("cid", rs.getString("cid"));
                map.put("rid", rs.getString("rid"));
                map.put("recipientsname", rs.getString("recipientsname"));
                map.put("fathername", rs.getString("fathername"));
                map.put("dob", rs.getString("dob"));
                map.put("age", rs.getString("age"));
                map.put("nric", rs.getString("nric"));
                map.put("passport", rs.getString("passport"));
                map.put("nationality", rs.getString("nationality"));
                map.put("organization", rs.getString("organization"));
                map.put("township", rs.getString("township"));
                map.put("division", rs.getString("division"));
                map.put("mobilephone", rs.getString("mobilephone"));
                map.put("batchrefcode", rs.getString("batchrefcode"));
                map.put("partnername", rs.getString("partnername"));
                map.put("partnerid", rs.getString("partnerid"));
                map.put("partnersyskey", rs.getString("partnersyskey"));
                map.put("voidstatus", rs.getString("voidstatus"));
                map.put("dose", rs.getString("dose"));
                map.put("gender", rs.getString("gender"));
                map.put("address1", rs.getString("address1"));
                map.put("prefixnrc", rs.getString("prefixnrc"));
                map.put("nrccode", rs.getString("nrccode"));
                map.put("nrctype", rs.getString("nrctype"));
                map.put("nrcno", rs.getString("nrcno"));
                map.put("ward", rs.getString("ward"));
                map.put("street", rs.getString("street"));
                map.put("occupation", rs.getString("occupation"));
                map.put("isexported", rs.getString("isexported"));
                map.put("vaccinationcenter", rs.getString("vaccinationcenter"));
                map.put("firstdosedate", "");
                map.put("firstdosedoctor", "");
                map.put("seconddosedate", "");
                map.put("seconddosedoctor", "");
                datalist.add(map);
            }
            if (datalist.size() > 0 && (!(dto.getOperator() == 0 && dto.getDosecount() == 0) || dto.isAlldose())) {
                String detailsql = String.format(
                        "select cid, doseupdatetime, lot, doctor, remark, userid from DoseRecords where cid in (%s) order by cid, doseupdatetime",
                        String.join(", ", cidlist));
                PreparedStatement detailstmt = connection.prepareStatement(detailsql);
                ResultSet drs = detailstmt.executeQuery();
                while (drs.next()) {
                    for (Map<String, Object> data : datalist) {
                        if (data.get("cid").equals(drs.getString("cid"))) {
                            String firstdosedate = (String) data.get("firstdosedate");
                            String seconddosedate = (String) data.get("seconddosedate");
                            if (firstdosedate.isEmpty()) {
                                data.put("firstdosedate", drs.getString("doseupdatetime"));
                                data.put("firstdosedoctor", drs.getString("doctor"));
                            } else if (seconddosedate.isEmpty()) {
                                data.put("seconddosedate", drs.getString("doseupdatetime"));
                                data.put("seconddosedoctor", drs.getString("doctor"));
                            }
                            break;
                        }
                    }
                }
            }

        }
        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        int totalcount = getTotalCount(query, args);
        res.setDatalist(datalist);
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());
        res.setPagecount((int) Math.ceil((double) totalcount / (double) dto.getPagesize()));
        res.setTotalcount(totalcount);
        return res;
    }

    public void saveExported(List<Map<String, Object>> datalist, String groupcode, String subgroupcode)
            throws SQLException {
        final String now = Instant.now().toString();
        getDBClient().insertMany("ExportedRecipients", datalist.stream().map(m -> {
            m.put("recipientsyskey", m.get("syskey"));
            m.put("syskey", KeyGenerator.generateSyskey());
            m.put("exporteddate", now);
            m.put("exportstatus", m.get("dose"));
            m.put("groupcode", groupcode);
            m.put("subgroupcode", subgroupcode);
            m.put("createddate", now);
            m.put("modifieddate", now);
            return m;
        }).collect(Collectors.toList()));
        if (datalist.size() > 0) {
            final String sql = String.format("update Recipients set isexported = 1 where cid in (%s)",
                    datalist.stream().map(m -> "'" + (String) m.get("cid") + "'").collect(Collectors.toList()));
            try (Connection connection = DbFactory.getConnection();
                    PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        }
    }
}
