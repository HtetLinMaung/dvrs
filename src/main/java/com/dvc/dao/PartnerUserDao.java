package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.dvc.utils.KeyGenerator;

import com.dvc.factory.DbFactory;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.PartnerUserDto;
import com.dvc.utils.ApiUtil;
import com.dvc.utils.EasySql;

public class PartnerUserDao extends BaseDao implements IPartnerUserDao {

    @Override
    public PaginationResponse<Map<String, Object>> getPartnerUsers(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("remark", "role", "dvrsuserid", "dvrsusername"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("syskey", "remark", "role", "dvrsuserid", "dvrsusername", "emailaddress",
                "dvrsuserid");
        String query = String.format("PartnerUser WHERE recordstatus <> 4 AND partnersyskey = %s  AND (%s)",
                dto.getPartnersyskey(), searchQuery);
        List<Map<String, Object>> datalist = new EasySql(DbFactory.getConnection()).getMany(keys, query,
                dto.getCurrentpage(), dto.getPagesize(), new ArrayList<>());
        PaginationResponse<Map<String, Object>> res = new PaginationResponse<>();
        res.setDatalist(datalist);
        res.setPagesize(dto.getPagesize());
        res.setCurrentpage(dto.getCurrentpage());
        int totalCount = (int) Math.ceil((double) getTotalCount(query, new ArrayList<>()));
        int pageCount = totalCount / dto.getPagesize();
        if ((totalCount % dto.getPagesize()) > 0) {
            pageCount += 1;
        }
        res.setPagecount(pageCount);
        res.setTotalcount((int) Math.ceil((double) getTotalCount(query, new ArrayList<>())));
        return res;
    }

    @Override
    public Map<String, Object> getPartnerUser(long syskey) throws SQLException {
        List<String> keys = Arrays.asList("syskey", "remark", "role", "dvrsuserid", "partnersyskey", "dvrsusername",
                "emailaddress", "dvrsuserid");
        String query = "PartnerUser WHERE recordstatus <> 4 AND syskey = ?";
        return new EasySql(DbFactory.getConnection()).getOne(keys, query, Arrays.asList(syskey));
    }

    @Override
    public int updatePartnerUser(PartnerUserDto dto) throws SQLException, IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", dto.getSyskey());
        args.put("modifieddate", Instant.now().toString());
        args.put("remark", dto.getRemark());
        args.put("role", dto.getRole());
        args.put("emailaddress", dto.getEmailaddress());
        args.put("dvrsusername", dto.getDvrsusername());
        ApiUtil.addOrUpdateIAMUser(dto, "PUT");
        return new EasySql(DbFactory.getConnection()).updateOne("PartnerUser", "syskey", args);
    }

    @Override
    public int deletePartnerUser(PartnerUserDto dto) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", dto.getSyskey());
        args.put("recordstatus", 4);
        args.put("modifieddate", Instant.now().toString());
        return new EasySql(DbFactory.getConnection()).updateOne("PartnerUser", "syskey", args);
    }

    @Override
    public long addPartnerUser(PartnerUserDto dto) throws SQLException, IOException {
        final long syskey = KeyGenerator.generateSyskey();
        final String now = Instant.now().toString();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", syskey);
        args.put("createddate", now);
        args.put("modifieddate", now);
        args.put("remark", dto.getRemark());
        args.put("role", dto.getRole());
        args.put("dvrsusername", dto.getDvrsusername());
        args.put("partnersyskey", dto.getPartnersyskey());
        args.put("emailaddress", dto.getEmailaddress());
        int[] result = new EasySql(DbFactory.getConnection()).insertMany("PartnerUser", Arrays.asList(args));
        if (result.length == 0 || result[0] == 0) {
            return 0;
        }

        ApiUtil.addOrUpdateIAMUser(dto, "POST");
        return syskey;
    }

}
