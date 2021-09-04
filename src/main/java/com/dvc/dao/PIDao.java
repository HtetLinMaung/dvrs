package com.dvc.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dvc.factory.DbFactory;
import com.dvc.models.FilterDto;
import com.dvc.models.PIDto;
import com.dvc.models.PaginationResponse;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;
import com.microsoft.azure.storage.StorageException;

public class PIDao extends BaseDao implements IPIDao {

    @Override
    public Map<String, Object> addPI(PIDto dto) throws SQLException, IOException {
        final long syskey = KeyGenerator.generateSyskey();
        final String now = Instant.now().toString();
        Map<String, Object> pi = new EasyData<PIDto>(dto).toMapExcept(Arrays.asList("batchsyskey", "statustype"));
        pi.put("syskey", syskey);
        pi.put("createddate", now);
        pi.put("modifieddate", now);
        pi.put("paymentstatus", 0);
        pi.put("recordstatus", 1);
        pi.put("balance", dto.getQty());
        pi.put("paymentdate", now);
        // Map<String, Object> partner = new
        // PartnerDao().getPartner(Long.parseLong((String) dto.getPartnersyskey()));
        Map<String, Object> center = new CenterDao().getCenter(dto.getCenterid());
        pi.put("price", center.get("price"));
        pi.put("amount", dto.getQty() * Double.parseDouble((String) center.get("price")));
        pi.put("total", pi.get("amount"));
        int[] results = new EasySql(DbFactory.getConnection()).insertMany("ProformaInvoice", Arrays.asList(pi));

        // if (results.length == 0 || results[0] == 0) {
        // return 0;
        // }
        Map<String, Object> newpi = getDBClient().getOne(Arrays.asList("autokey", "syskey"),
                "ProformaInvoice where syskey = ?", Arrays.asList(syskey));
        newpi.put("pirefnumber", String.format("PI%06d", Integer.parseInt((String) newpi.get("autokey"))));
        newpi.remove("autokey");
        int result = getDBClient().updateOne("ProformaInvoice", "syskey", newpi);
        // if (result == 0) {
        // return 0;
        // }

        Map<String, Object> map = new HashMap<>();
        map.put("syskey", syskey);
        map.put("pirefnumber", newpi.get("pirefnumber"));
        return map;
    }

    @Override
    public int updatePI(PIDto dto) throws SQLException, IOException {
        final String now = Instant.now().toString();
        Map<String, Object> pi = new EasyData<PIDto>(dto).toMapExcept(
                Arrays.asList("batchsyskey", "statustype", "recordstatus", "pirefnumber", "price", "paymentdate"));
        pi.put("modifieddate", now);
        Map<String, Object> oldpi = getPi(Long.parseLong(dto.getSyskey()));
        if (oldpi.get("recordstatus").equals("30")) {
            pi.remove("qty");
            pi.remove("price");
            pi.remove("amount");
            pi.remove("total");
            pi.remove("centerid");
        } else {
            // Map<String, Object> partner = new
            // PartnerDao().getPartner(Long.parseLong((String) dto.getPartnersyskey()));
            Map<String, Object> center = new CenterDao().getCenter(dto.getCenterid());
            pi.put("amount", dto.getQty() * Double.parseDouble((String) center.get("price")));
            pi.put("total", pi.get("amount"));
            pi.put("balance", dto.getQty());
        }
        return new EasySql(DbFactory.getConnection()).updateOne("ProformaInvoice", "syskey", pi);
    }

    public int updatePI(Map<String, Object> pi) throws SQLException {
        final String now = Instant.now().toString();
        pi.put("modifieddate", now);
        return new EasySql(DbFactory.getConnection()).updateOne("ProformaInvoice", "syskey", pi);
    }

    @Override
    public Map<String, Object> getPIByBatch(long batchsyskey) throws SQLException, IOException {
        List<String> keys = new EasyData<PIDto>(new PIDto()).toMap().entrySet().stream().map(pair -> pair.getKey())
                .collect(Collectors.toList());
        keys.remove("syskey");
        keys.remove("remark");
        keys.remove("partnersyskey");
        keys.add("p.syskey");
        keys.add("p.remark");
        keys.add("p.partnersyskey");
        keys.remove("batchsyskey");

        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(keys,
                "ProformaInvoice as p left join BatchUpload as b on b.pisyskey = p.syskey where b.syskey = ?",
                Arrays.asList(batchsyskey));

        if (!map.containsKey("syskey")) {
            map.put("syskey", "");
        }

        return map;
    }

    @Override
    public PaginationResponse<Map<String, Object>> getPis(FilterDto dto) throws SQLException, IOException {
        String searchQuery = EasySql.generateSearchQuery(
                Arrays.asList("pirefnumber", "paymentref", "bankname", "p.centerid"), dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = new EasyData<PIDto>(new PIDto()).toMapExcept(
                Arrays.asList("batchsyskey", "statustype", "syskey", "remark", "centerid", "recordstatus", "price",
                        "branch", "total", "contact", "paymentstatus", "applicantcount", "attention", "taxamount"))
                .entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        keys.add("p.syskey");
        keys.add("centername");
        keys.add("partnername");
        keys.add("p.centerid");
        keys.add("p.recordstatus");
        keys.add("partnerid");
        keys.add("balance");
        keys.add("voidcount");

        String query = "";
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (dto.getRole().equals("Admin") || dto.getRole().equals("Finance")) {
            query = String.format(
                    "ProformaInvoice as p left join Centers as c on c.centerid = p.centerid left join Partners as pr on pr.syskey = p.partnersyskey WHERE p.recordstatus <> 4 %s %s %s and (%s)",
                    dto.getPartnersyskey().isEmpty() ? "" : "and partnersyskey = ?",
                    dto.getCenterid().isEmpty() ? "" : "and p.centerid = '" + dto.getCenterid() + "'",
                    dto.getRecordstatus().isEmpty() ? "" : "and p.recordstatus = " + dto.getRecordstatus(),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "pirefnumber", true,
                    dto.getCurrentpage(), dto.getPagesize(),
                    dto.getPartnersyskey().isEmpty() ? new ArrayList<>() : Arrays.asList(dto.getPartnersyskey()));
        } else {
            query = String.format(
                    "ProformaInvoice as p left join Centers as c on c.centerid = p.centerid left join Partners as pr on pr.syskey = p.partnersyskey WHERE p.recordstatus <> 4 AND p.partnersyskey = ? %s %s and (%s)",
                    dto.getCenterid().isEmpty() ? "" : "and p.centerid = '" + dto.getCenterid() + "'",
                    dto.getRecordstatus().isEmpty() ? "" : "and p.recordstatus = " + dto.getRecordstatus(),
                    searchQuery);
            datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query, "pirefnumber", true,
                    dto.getCurrentpage(), dto.getPagesize(), Arrays.asList(dto.getPartnersyskey()));
        }

        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        res.setDatalist(datalist.stream().map(m -> {
            // try {
            // int currentcount = getTotalCount("Recipients where pisyskey = ? and
            // batchuploadsyskey <> 0",
            // Arrays.asList(Long.parseLong((String) m.get("syskey"))));
            // int qty = Integer.parseInt((String) m.get("qty"));
            // int remaining = qty - currentcount;
            // if (remaining < 0) {
            // remaining = 0;
            // }
            // m.put("balance", remaining);
            // } catch (Exception e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            m.put("balance", String.valueOf(
                    Integer.parseInt((String) m.get("voidcount")) + Integer.parseInt((String) m.get("balance"))));
            m.put("recordstatusdesc", m.get("recordstatus").equals("30") ? "Approved" : "Submitted");
            // String paymentdesc = "-";
            // switch ((String) m.get("paymentstatus")) {
            // case "1":
            // paymentdesc = "Processing";
            // break;
            // case "10":
            // paymentdesc = "Paid";
            // break;
            // }
            // m.remove("paymentstatus");
            // m.put("paymentdesc", paymentdesc);
            return m;
        }).collect(Collectors.toList()));
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
    public Map<String, Object> getPi(long syskey) throws SQLException, IOException {
        List<String> keys = new EasyData<PIDto>(new PIDto()).toMapExcept(
                Arrays.asList("batchsyskey", "statustype", "syskey", "remark", "centerid", "recordstatus", "price"))
                .entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        keys.add("p.syskey");
        keys.add("centername");
        keys.add("partnername");
        keys.add("p.remark");
        keys.add("p.centerid");
        keys.add("p.recordstatus");
        keys.add("partnerid");
        keys.add("p.price");
        keys.add("emailaddress");
        keys.add("balance");
        keys.add("voidcount");
        Map<String, Object> map = new EasySql(DbFactory.getConnection()).getOne(keys,
                "ProformaInvoice as p left join Centers as c on c.centerid = p.centerid left join Partners as pr on pr.syskey = p.partnersyskey where p.syskey = ?",
                Arrays.asList(syskey));
        map.put("recordstatusdesc", map.get("recordstatus").equals("30") ? "Approved" : "Submitted");
        String paymentdesc = "-";
        switch ((String) map.get("paymentstatus")) {
            case "1":
                paymentdesc = "Processing";
                break;
            case "10":
                paymentdesc = "Paid";
                break;
        }
        // map.put("balance", new CenterDao().getRemainingCount(Long.parseLong((String)
        // map.get("syskey"))));

        map.put("paymentdesc", paymentdesc);
        List<Map<String, Object>> attachmentlist = new AttachmentDao()
                .getAttachmentsByPi(Long.parseLong((String) map.get("syskey")));

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
    public String getPartnerEmail(long syskey) throws SQLException {
        return (String) getDBClient().getOne(Arrays.asList("emailaddress"),
                "ProformaInvoice as pi left join Partners as p on p.syskey = pi.partnersyskey where pi.syskey = ?",
                Arrays.asList(syskey)).get("emailaddress");
    }

    public boolean isOwnPi(String pisyskey, String partnersyskey) throws SQLException {
        int total = getTotalCount("ProformaInvoice where recordstatus <> 4 and syskey = ? and partnersyskey = ?",
                Arrays.asList(pisyskey, partnersyskey));
        if (total > 0) {
            return true;
        }
        return false;
    }

    public Map<String, Object> getTotalQB(String partnersyskey, String centerid) throws SQLException {
        String sql = "select sum(qty) as totalqty, sum(balance + voidcount) as totalbalance from [dbo].[ProformaInvoice]";
        if (!partnersyskey.isEmpty() && !centerid.isEmpty()) {
            sql += " where partnersyskey = ? and centerid = ?";
        } else if (!partnersyskey.isEmpty()) {
            sql += " where partnersyskey = ?";
        } else if (!centerid.isEmpty()) {
            sql += " where centerid = ?";
        }

        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (!partnersyskey.isEmpty() && !centerid.isEmpty()) {
                stmt.setString(1, partnersyskey);
                stmt.setString(2, centerid);
            } else if (!partnersyskey.isEmpty()) {
                stmt.setString(1, partnersyskey);
            } else if (!centerid.isEmpty()) {
                stmt.setString(1, centerid);
            }

            ResultSet rs = stmt.executeQuery();
            Map<String, Object> data = new HashMap<>();
            while (rs.next()) {
                data.put("totalqty", rs.getString("totalqty"));
                data.put("totalbalance", rs.getString("totalbalance"));
            }
            return data;

        }
    }

    public List<Map<String, Object>> getPiUsagesOnCenter() throws SQLException {
        String sql = "select pi.centerid, c.centername, sum(qty) as totalqty, sum(balance + voidcount) as totalbalance, sum(qty) - sum(balance + voidcount) as totalusedamount from [dbo].[ProformaInvoice] as pi left join Centers as c on c.centerid = pi.centerid  group by pi.centerid, c.centername";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("centername", rs.getString("centername"));
                data.put("centerid", rs.getString("centerid"));
                data.put("totalqty", rs.getInt("totalqty"));
                data.put("totalbalance", rs.getInt("totalbalance"));
                data.put("totalusedamount", rs.getInt("totalusedamount"));
                datalist.add(data);
            }
            return datalist;
        }
    }

    public List<Map<String, Object>> getPartnerPiUsages() throws SQLException {
        String sql = "select partnername, partnerid, sum(qty) as totalqty, sum(balance + voidcount) as totalbalance, sum(qty) - sum(balance + voidcount) as totalusedamount from [dbo].[ProformaInvoice] as pi left join [dbo].[Partners] as p on p.syskey = pi.partnersyskey group by partnersyskey, partnername, partnerid";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("partnerid", rs.getString("partnerid"));
                data.put("partnername", rs.getString("partnername"));
                data.put("totalqty", rs.getInt("totalqty"));
                data.put("totalbalance", rs.getInt("totalbalance"));
                data.put("totalusedamount", rs.getInt("totalusedamount"));
                datalist.add(data);
            }
            return datalist;
        }
    }

    public List<Map<String, Object>> getPartnerPiUsagesByCenter(String centerid) throws SQLException {
        String sql = "select partnername, partnerid, sum(qty) as totalqty, sum(balance + voidcount) as totalbalance, sum(qty) - sum(balance + voidcount) as totalusedamount from [dbo].[ProformaInvoice] as pi left join [dbo].[Partners] as p on p.syskey = pi.partnersyskey where centerid = ? group by partnersyskey, partnername, partnerid";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, centerid);
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("partnerid", rs.getString("partnerid"));
                data.put("partnername", rs.getString("partnername"));
                data.put("totalqty", rs.getInt("totalqty"));
                data.put("totalbalance", rs.getInt("totalbalance"));
                data.put("totalusedamount", rs.getInt("totalusedamount"));
                datalist.add(data);
            }
            return datalist;
        }
    }

    public List<Map<String, Object>> getPiUsagesOnCenterByPartner(String partnersyskey) throws SQLException {
        String sql = "select pi.centerid, c.centername, sum(qty) as totalqty, sum(balance + voidcount) as totalbalance, sum(qty) - sum(balance + voidcount) as totalusedamount from [dbo].[ProformaInvoice] as pi left join Centers as c on c.centerid = pi.centerid where partnersyskey = ? group by pi.centerid, c.centername";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, partnersyskey);
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("centername", rs.getString("centername"));
                data.put("centerid", rs.getString("centerid"));
                data.put("totalqty", rs.getInt("totalqty"));
                data.put("totalbalance", rs.getInt("totalbalance"));
                data.put("totalusedamount", rs.getInt("totalusedamount"));
                datalist.add(data);
            }
            return datalist;
        }
    }
}
