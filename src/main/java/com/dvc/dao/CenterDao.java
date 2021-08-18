package com.dvc.dao;

import java.io.IOException;
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

import com.dvc.factory.DbFactory;
import com.dvc.models.CenterDto;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;

public class CenterDao extends BaseDao implements ICenterDao {

    @Override
    public List<Map<String, Object>> getCenters() throws SQLException {
        return getDBClient().getMany(Arrays.asList("centerid", "centername", "allowblank", "price"),
                "Centers where recordstatus <> 4");
    }

    private String sum(List<String> datalist) {
        int total = datalist.stream().map(data -> Integer.parseInt(data.replaceAll("^([a-zA-Z]{1,3}[0-9])", "")))
                .reduce(0, (subtotal, element) -> {
                    return subtotal + element;
                });
        return String.format("%07d", total);
    }

    private List<String> generateSerialRange(String lastcid, int count, String centerid) {
        List<String> datalist = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            datalist.add(centerid + sum(Arrays.asList(lastcid, String.valueOf(i))));
        }
        return datalist;
    }

    @Override
    public List<String> addLastSerial(String centerid, int count) throws SQLException {
        String sql = "select cid from CenterLastSerials where centerid = ?";
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, centerid);
            ResultSet rs = stmt.executeQuery();
            String lastcid = "";
            String newlastcid = "0";
            final String now = Instant.now().toString();
            while (rs.next()) {
                lastcid = rs.getString("cid");
            }
            if (lastcid.isEmpty()) {
                sql = "insert into CenterLastSerials (syskey, centerid, cid, createddate, modifieddate) values (?, ?, ?, ?, ?)";
                PreparedStatement insertstmt = connection.prepareStatement(sql);
                int i = 1;
                for (Object value : Arrays.asList(KeyGenerator.generateSyskey(), centerid,
                        centerid + sum(Arrays.asList("0000000", String.valueOf(count))), now, now)) {
                    insertstmt.setObject(i++, value);
                }
                insertstmt.executeUpdate();
                return generateSerialRange(newlastcid, count, centerid);
            } else {
                sql = "update CenterLastSerials set cid = ? where centerid = ?";
                PreparedStatement updatestmt = connection.prepareStatement(sql);
                newlastcid = centerid + sum(Arrays.asList(lastcid, String.valueOf(count)));
                updatestmt.setString(1, newlastcid);
                updatestmt.setString(2, centerid);
                updatestmt.executeUpdate();
                return generateSerialRange(lastcid, count, centerid);
            }

        }

    }

    @Override
    public int getRemainingCount(long pisyskey) throws SQLException, IOException {
        int currentcount = getTotalCount("Recipients where pisyskey = ? and batchuploadsyskey <> 0",
                Arrays.asList(pisyskey));

        Map<String, Object> pi = new PIDao().getPi(pisyskey);
        int totalcount = Integer.parseInt((String) pi.get("qty"));

        int remaining = totalcount - currentcount;
        if (remaining < 0) {
            return 0;
        }
        return remaining;
    }

    @Override
    public Map<String, Object> getCenter(String centerid) throws SQLException {
        return getDBClient().getOne(Arrays.asList("syskey", "centerid", "centername", "allowblank", "price"),
                "Centers where recordstatus <> 4 and centerid = ?", Arrays.asList(centerid));
    }

    public Map<String, Object> getCenter(long syskey) throws SQLException {
        return getDBClient().getOne(Arrays.asList("syskey", "centerid", "centername", "allowblank", "price"),
                "Centers where recordstatus <> 4 and syskey = ?", Arrays.asList(syskey));
    }

    @Override
    public PaginationResponse<Map<String, Object>> getCenters(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("centerid", "centername"), dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("syskey", "centerid", "centername", "allowblank", "price");
        String query = String.format("Centers where recordstatus <> 4 and (%s)", searchQuery);
        List<Map<String, Object>> datalist = getDBClient().getMany(keys, query, dto.getCurrentpage(),
                dto.getPagesize());

        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        res.setDatalist(datalist);
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());
        res.setPagecount((int) Math.ceil((double) getTotalCount(query) / (double) dto.getPagesize()));
        res.setTotalcount(datalist.size());
        return res;
    }

    @Override
    public long addCenter(CenterDto dto) throws SQLException, IOException {
        final String now = Instant.now().toString();
        final long syskey = KeyGenerator.generateSyskey();
        Map<String, Object> center = new EasyData<CenterDto>(dto).toMap();
        center.put("syskey", syskey);
        center.put("createddate", now);
        center.put("modifieddate", now);
        int[] result = getDBClient().insertMany("Centers", Arrays.asList(center));
        if (result.length == 0 || result[0] == 0) {
            return 0;
        }
        return syskey;
    }

    @Override
    public int updateCenter(CenterDto dto) throws SQLException, IOException {
        final String now = Instant.now().toString();
        Map<String, Object> center = new EasyData<CenterDto>(dto).toMap();
        center.put("modifieddate", now);
        return getDBClient().updateOne("Centers", "syskey", center);
    }

    @Override
    public int deleteCenter(CenterDto dto) throws SQLException {
        final String now = Instant.now().toString();
        Map<String, Object> center = new HashMap<>();
        center.put("modifieddate", now);
        center.put("syskey", dto.getSyskey());
        center.put("recordstatus", 4);
        return getDBClient().updateOne("Centers", "syskey", center);
    }

    public boolean isCenterValid(String centerid) throws SQLException {
        int total = getTotalCount("Centers where centerid = ?", Arrays.asList(centerid));
        if (total > 0)
            return true;
        return false;
    }
}
