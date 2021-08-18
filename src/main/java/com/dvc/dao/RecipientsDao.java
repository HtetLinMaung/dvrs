package com.dvc.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dvc.factory.DbFactory;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.RecipientsDto;
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

    @Override
    public PaginationResponse<Map<String, Object>> getRecipients(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("cid", "recipientsname", "nric"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("r.syskey", "rid", "cid", "recipientsname", "fathername", "dob", "age",
                "nric", "passport", "nationality", "organization", "township", "division", "mobilephone",
                "registerationstatus", "vaccinationstatus", "batchrefcode", "partnername", "partnerid",
                "r.partnersyskey", "voidstatus", "dose");
        String query = "";
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getRole().equals("Admin") || dto.getRole().equals("Finance")) {
            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey WHERE r.recordstatus <> 4 %s and (%s)",
                    getFilterQuery(dto), searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "cid", true, dto.getCurrentpage(),
                    dto.getPagesize(),
                    dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        } else {
            query = String.format(
                    "Recipients as r left join Partners as p on r.partnersyskey = p.syskey WHERE r.recordstatus <> 4 AND r.partnersyskey = ? %s and (%s)",
                    !dto.getCenterid().isEmpty() ? "and cid like " + "'" + dto.getCenterid() + "%'" : "", searchQuery);
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
        if (status == 0) {
            args = getDBClient().getOne(Arrays.asList("p.syskey", "voidcount"),
                    "Recipients as r left join ProformaInvoice as p on r.pisyskey = p.syskey where r.syskey = ?",
                    Arrays.asList(syskey));
            if (args.get("syskey") == null) {
                return 0;
            }
            args.put("voidcount", Integer.parseInt((String) args.get("voidcount")) + voidcount);
            return getDBClient().updateOne("ProformaInvoice", "syskey", args);
        }
        return voidcount;
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
            return getDBClient().updateOne("Recipients", "batchuploadsyskey", args);
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
}
