package com.dvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dvc.factory.DbFactory;
import com.dvc.utils.EasySql;

public class BaseDao implements IBaseDao {
    @Override
    public int getTotalCount(String tableName) throws SQLException {
        return getTotalCount(tableName, new ArrayList<>());
    }

    public int getTotalCount(String tableName, List<Object> params) throws SQLException {
        final String sql = String.format("SELECT COUNT(*) AS total FROM %s", tableName);
        try (Connection connection = DbFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            for (Object param : params) {
                stmt.setObject(i++, param);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }

    public EasySql getDBClient() throws SQLException {
        return new EasySql(DbFactory.getConnection());
    }

}
