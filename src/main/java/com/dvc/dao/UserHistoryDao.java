package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.dvc.factory.DbFactory;
import com.dvc.models.UserHistoryDto;
import com.dvc.utils.EasySql;
import com.dvc.utils.KeyGenerator;
import java.util.Arrays;

public class UserHistoryDao {
    private static UserHistoryDao userHistoryDao;

    public static UserHistoryDao getInstance() {
        if (userHistoryDao == null) {
            userHistoryDao = new UserHistoryDao();
        }
        return userHistoryDao;
    }

    private UserHistoryDao() {
    }

    public long addUserHistory(UserHistoryDto dto) throws SQLException, IOException {
        final long syskey = KeyGenerator.generateSyskey();
        final String now = Instant.now().toString();
        // EasyData<PartnerDto> easyData = new EasyData<>(dto);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", syskey);
        args.put("createddate", now);
        args.put("modifieddate", now);
        args.put("n2", dto.getType());

        if (dto.getType() == 1) {
            args.put("t1", dto.getPartnerDto().getUserid());
            args.put("t2", dto.getPartnerDto().getUsername());
            args.put("t8", dto.getPartnerDto().getRemark());
            args.put("n1", dto.getPartnerDto().getPartnertype());
            args.put("t3", dto.getPartnerDto().getPartnername());
            args.put("t7", dto.getPartnerDto().getAddress());
            args.put("t6", dto.getPartnerDto().getContactperson());
            args.put("t5", dto.getPartnerDto().getEmailaddress());
            args.put("t4", dto.getPartnerDto().getContactnumber());
            args.put("n3", dto.getPartnerDto().getSyskey());
            args.put("t10", dto.getPartnerDto().getBrowserinfo());
        } else if (dto.getType() == 2) {
            args.put("t1", dto.getPartnerUserDto().getUserid());
            args.put("t2", dto.getPartnerUserDto().getUsername());
            args.put("t8", dto.getPartnerUserDto().getRemark());
            args.put("t9", dto.getPartnerUserDto().getRole());
            args.put("t12", dto.getPartnerUserDto().getDvrsusername());
            args.put("n3", dto.getPartnerUserDto().getPartnersyskey());
            args.put("t5", dto.getPartnerUserDto().getEmailaddress());
            args.put("n4", dto.getPartnerUserDto().getSyskey());
            args.put("t10", dto.getPartnerUserDto().getBrowserinfo());
        }
        args.put("t11", dto.getIpaddress());

        int[] result = new EasySql(DbFactory.getConnection()).insertMany("UserHistory", Arrays.asList(args));
        if (result.length == 0 || result[0] == 0) {
            return 0;
        }
        return syskey;
    }

    public int updateUserHistory(UserHistoryDto dto) throws SQLException, IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("syskey", dto.getSyskey());
        args.put("modifieddate", Instant.now().toString());
        if (dto.getType() == 1) {
            args.put("n3", dto.getPartnerDto().getSyskey());
        } else if (dto.getType() == 2) {
            args.put("n4", dto.getPartnerUserDto().getSyskey());
        }

        return new EasySql(DbFactory.getConnection()).updateOne("UserHistory", "syskey", args);
    }
}
