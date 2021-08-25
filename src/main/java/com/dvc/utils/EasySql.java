package com.dvc.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EasySql {
    private Connection connection;

    public EasySql(Connection connection) {
        this.connection = connection;
    }

    public static String generateSearchQuery(List<String> keys, String search) {
        // if (search.trim().split("/").length == 3) {
        // if (search.startsWith("\"") && search.endsWith("\"")) {
        // return String.format("FORMAT(CreatedDate,'dd/MM/yyyy') = %s", "'%" + search +
        // "%'");
        // }
        // return String.format("FORMAT(CreatedDate,'dd/MM/yyyy') LIKE %s", "'%" +
        // search + "%'");
        // }
        // if (search.startsWith("\"") && search.endsWith("\"")) {
        // return String.join(" OR ",
        // keys.stream().map(
        // k -> String.format("CONVERT(varchar(255), %s) = N'%s'", k,
        // search.replaceAll("\"", "")))
        // .collect(Collectors.toList()));
        // }

        // return String.join(" OR ",
        // keys.stream().map(k -> String.format("CONVERT(varchar(255), %s) LIKE %s", k,
        // "N'%" + search + "%'"))
        // .collect(Collectors.toList()));

        if (search.startsWith("\"") && search.endsWith("\"")) {
            return String.join(" OR ",
                    keys.stream().map(k -> String.format("%s = N'%s'", k, search.replaceAll("\"", "")))
                            .collect(Collectors.toList()));
        }

        return String.join(" OR ", keys.stream().map(k -> String.format("%s LIKE %s", k, "N'%" + search + "%'"))
                .collect(Collectors.toList()));
    }

    public static List<String> generateKeys(List<String> keys) {
        return generateKeys(keys, "");
    }

    public static List<String> generateKeys(List<String> keys, String alias) {
        List<String> newKeys = new ArrayList<>();
        newKeys.add(alias + "syskey");
        newKeys.add(alias + "createddate");
        newKeys.add(alias + "modifieddate");
        newKeys.add(alias + "syncstatus");
        newKeys.add(alias + "syncbatch");
        newKeys.add(alias + "userid");
        newKeys.add(alias + "username");
        newKeys.add(alias + "recordstatus");
        newKeys.addAll(keys);
        return newKeys;
    }

    public int copyTo(List<String> keys, String tableName, String query) throws SQLException {
        final String sql = String.format("SELECT %s INTO %s FROM %s", String.join(", ", keys), tableName, query);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            int result = stmt.executeUpdate();
            connection.close();
            return result;
        }
    }

    public Map<String, Object> getOne(List<String> keys, String query) throws SQLException {
        return getOne(keys, query, new ArrayList<>());
    }

    public Map<String, Object> getOne(List<String> keys, String query, List<Object> params) throws SQLException {
        List<Map<String, Object>> datalist = getMany(keys, query, params);
        if (datalist.size() > 0) {
            return datalist.get(0);
        } else {
            Map<String, Object> map = new HashMap<>();
            for (String key : keys) {
                map.put(key, "");
            }
            return map;
        }
    }

    public List<Map<String, Object>> getMany(List<String> keys, String query) throws SQLException {
        return getMany(keys, query, new ArrayList<>());
    }

    public List<Map<String, Object>> getMany(List<String> keys, String query, List<Object> params) throws SQLException {
        final String sql = String.format("SELECT %s FROM %s", String.join(", ", keys), query);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            for (Object param : params) {
                stmt.setObject(i++, param);
            }
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (String key : keys) {
                    if (key.split("\\.").length > 1) {
                        map.put(key.split("\\.")[1], rs.getString(key.split("\\.")[1]));
                    } else {
                        map.put(key, rs.getString(key));
                    }
                }
                datalist.add(map);
            }
            connection.close();
            return datalist;
        }
    }

    public List<Map<String, Object>> getMany(List<String> keys, String query, int page, int pageSize,
            List<Object> params) throws SQLException {
        return getMany(keys, query, "modifieddate", true, page, pageSize, params);

    }

    public List<Map<String, Object>> getMany(List<String> keys, String query, int page, int pageSize)
            throws SQLException {
        return getMany(keys, query, "modifieddate", true, page, pageSize, new ArrayList<>());

    }

    public List<Map<String, Object>> getMany(List<String> keys, String query, String sortBy, boolean reverse, int page,
            int pageSize, List<Object> params) throws SQLException {
        final String sql = String.format("SELECT %s FROM %s ORDER BY %s %s OFFSET ? ROWS FETCH FIRST ? ROWS ONLY",
                String.join(", ", keys), query, sortBy, reverse ? "DESC" : "");
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            int offset = (page - 1) * pageSize;
            for (Object param : params) {
                stmt.setObject(i++, param);
            }
            stmt.setObject(i++, offset);
            stmt.setObject(i++, pageSize);

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> datalist = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (String key : keys) {
                    if (key.split("\\.").length > 1) {
                        map.put(key.split("\\.")[1], rs.getString(key.split("\\.")[1]));
                    } else {
                        map.put(key, rs.getString(key));
                    }
                }

                datalist.add(map);
            }
            connection.close();
            return datalist;
        }

    }

    public int[] insertMany(String tableName, List<Map<String, Object>> args) throws SQLException {
        List<String> keys = new ArrayList<>();
        if (args.size() > 0) {
            keys = args.get(0).entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        }
        final String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(", ", keys),
                String.join(", ", keys.stream().map(k -> "?").collect(Collectors.toList())));
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            for (Map<String, Object> map : args) {
                for (int i = 0; i < keys.size(); i++) {
                    stmt.setObject(i + 1, map.get(keys.get(i)));
                }
                stmt.addBatch();
            }

            int[] result = stmt.executeBatch();
            connection.close();
            return result;
        }
    }

    public int insertOne(String tableName, List<String> keys, List<Object> args) throws SQLException {
        final String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(", ", keys),
                String.join(", ", keys.stream().map(k -> "?").collect(Collectors.toList())));
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            for (int i = 0; i < args.size(); i++) {
                stmt.setObject(i + 1, args.get(i));
            }
            int result = stmt.executeUpdate();
            connection.close();
            return result;
        }
    }

    public int upsertOne(String tableName, String idFieldName, Map<String, Object> arg) throws SQLException {
        final List<String> keys = arg.entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        final String sql = String.format(
                "IF ( (SELECT COUNT(*) FROM %s WHERE %s = ?) > 0 )) UPDATE %s SET %s WHERE %s = ? ELSE INSERT INTO %s (%s) VALUES (%s)",
                tableName, idFieldName, tableName,
                String.join(", ", keys.stream().map(k -> k + " = ?").collect(Collectors.toList())), idFieldName,
                tableName, String.join(", ", keys),
                String.join(", ", keys.stream().map(k -> "?").collect(Collectors.toList())));
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            stmt.setObject(i++, arg.get(idFieldName));
            for (String key : keys) {
                stmt.setObject(i++, arg.get(key));
            }
            stmt.setObject(i++, arg.get(idFieldName));
            for (String key : keys) {
                stmt.setObject(i++, arg.get(key));
            }
            int result = stmt.executeUpdate();
            connection.close();
            return result;
        }

    }

    public int deleteOne(String tableName, String idFieldName, Object value) throws SQLException {
        final String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, idFieldName);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setObject(1, value);
            int result = stmt.executeUpdate();
            connection.close();
            return result;
        }
    }

    public int[] deleteMany(String tableName, String idFieldName, List<Object> values) throws SQLException {
        final String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, idFieldName);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            for (Object value : values) {
                stmt.setObject(1, value);
                stmt.addBatch();
            }
            int[] result = stmt.executeBatch();
            connection.close();
            return result;
        }
    }

    public int updateOne(String tableName, String idFieldName, Map<String, Object> args) throws SQLException {
        List<String> keys = args.entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        final String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName,
                String.join(", ", keys.stream().map(k -> k + " = ?").collect(Collectors.toList())), idFieldName);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 1;
            for (String key : keys) {
                stmt.setObject(i++, args.get(key));
            }
            stmt.setObject(i, args.get(idFieldName));
            int result = stmt.executeUpdate();
            connection.close();
            return result;
        }

    }

    public int[] updateMany(String tableName, String idFieldName, List<Object> values, Map<String, Object> args)
            throws SQLException {
        List<String> keys = args.entrySet().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
        final String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName,
                String.join(", ", keys.stream().map(k -> k + " = ?").collect(Collectors.toList())), idFieldName);
        try (PreparedStatement stmt = connection.prepareStatement(sql);) {
            for (Object value : values) {
                int i = 1;
                for (String key : keys) {
                    stmt.setObject(i++, args.get(key));
                }
                stmt.setObject(i, value);
                stmt.addBatch();
            }

            int[] result = stmt.executeBatch();
            connection.close();
            return result;
        }
    }

    // List<Map<String, Object>> select(String tableName, List<String> keys) throws
    // SQLException {
    // final String sql = String.format("SELECT %s FROM %s", tableName,
    // String.join(", ", keys));
    // try (PreparedStatement stmt = connection.prepareStatement(sql);) {
    // ResultSet rs =
    // }
    // }

}
