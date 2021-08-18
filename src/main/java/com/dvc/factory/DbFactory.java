package com.dvc.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbFactory {
    private static final String connectionString = System.getenv("AzureSQLConnectionString");

    public static Connection getConnection() throws SQLException {
        return getConnection(connectionString);
    }

    private static Connection getConnection(String connString) throws SQLException {
        try {
            return DriverManager.getConnection(connString);
        } catch (Exception e) {
            return DriverManager.getConnection(connString);
        }
    }
}
