package com.dvc.dao;

import java.sql.SQLException;
import java.util.Arrays;
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

}
