package com.dvc.dao;

import java.sql.SQLException;
import java.util.Map;

public interface ILogDao {
    int addVerifyLog(Map<String, Object> data) throws SQLException;
}
