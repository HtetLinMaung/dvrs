package com.dvc.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LogDao extends BaseDao implements ILogDao {

    @Override
    public int addVerifyLog(Map<String, Object> data) throws SQLException {
        int[] result = getDBClient().insertMany("QRLog", Arrays.asList(data));
        if (result.length == 0) {
            return 0;
        }
        return result[0];
    }

    public Map<String, Object> getQRSummary(String userid) throws SQLException {
        int verifycount = getTotalCount("QRLog where userid = ?", Arrays.asList(userid));
        int updatecount = getTotalCount("DoseRecord where userid = ?", Arrays.asList(userid));
        Map<String, Object> summary = new HashMap<>();
        summary.put("verifycount", verifycount);
        summary.put("updatecount", updatecount);
        return summary;
    }

}
