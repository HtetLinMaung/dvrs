package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.dvc.factory.DbFactory;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.PartnerDto;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;

public class PartnerDao extends BaseDao implements IPartnerDao {

    @Override
    public PaginationResponse<Map<String, Object>> getPartners(FilterDto dto) throws SQLException {
        String searchQuery = EasySql.generateSearchQuery(Arrays.asList("remark", "partnertype", "partnername",
                "address", "contactperson", "emailaddress", "contactnumber", "partnerid", "partnertype"),
                dto.getSearch());
        if (dto.getSearch().isEmpty()) {
            searchQuery = "1 = 1";
        }
        List<String> keys = Arrays.asList("syskey", "remark", "emailaddress", "partnername", "contactnumber",
                "partnertype", "address", "contactperson", "partnerid", "price");
        String query = String.format("Partners WHERE recordstatus <> 4 and (%s)", searchQuery);
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
    public Map<String, Object> getPartner(long syskey) throws SQLException {
        List<String> keys = Arrays.asList("syskey", "partnername", "partnertype", "address", "contactperson",
                "emailaddress", "contactnumber", "remark", "partnerid", "price");
        String query = "Partners WHERE recordstatus <> 4 AND syskey = ?";

        return new EasySql(DbFactory.getConnection()).getOne(keys, query, Arrays.asList(syskey));
    }

    @Override
    public int updatePartner(PartnerDto dto) throws SQLException, IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", dto.getSyskey());
        args.put("modifieddate", Instant.now().toString());
        args.put("remark", dto.getRemark());
        args.put("partnertype", dto.getPartnertype());
        args.put("partnername", dto.getPartnername());
        args.put("address", dto.getAddress());
        args.put("contactperson", dto.getContactperson());
        args.put("emailaddress", dto.getEmailaddress());
        args.put("contactnumber", dto.getContactnumber());
        return new EasySql(DbFactory.getConnection()).updateOne("Partners", "syskey", args);
    }

    @Override
    public int deletePartner(PartnerDto dto) throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("syskey", dto.getSyskey());
        args.put("recordstatus", 4);
        args.put("modifieddate", Instant.now().toString());
        return new EasySql(DbFactory.getConnection()).updateOne("Partners", "syskey", args);
    }

    @Override
    public long addPartner(PartnerDto dto) throws SQLException, IOException {
        final long syskey = KeyGenerator.generateSyskey();
        final String now = Instant.now().toString();
        // EasyData<PartnerDto> easyData = new EasyData<>(dto);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", syskey);
        args.put("createddate", now);
        args.put("modifieddate", now);
        args.put("remark", dto.getRemark());
        args.put("partnertype", dto.getPartnertype());
        args.put("partnername", dto.getPartnername());
        args.put("address", dto.getAddress());
        args.put("contactperson", dto.getContactperson());
        args.put("emailaddress", dto.getEmailaddress());
        args.put("contactnumber", dto.getContactnumber());

        int[] result = new EasySql(DbFactory.getConnection()).insertMany("Partners", Arrays.asList(args));
        if (result.length == 0 || result[0] == 0) {
            return 0;
        }
        return syskey;
    }

}
