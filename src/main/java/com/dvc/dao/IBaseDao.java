package com.dvc.dao;

import java.sql.SQLException;

public interface IBaseDao {
    int getTotalCount(String tableName) throws SQLException;
}
