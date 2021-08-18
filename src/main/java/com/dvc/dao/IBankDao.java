package com.dvc.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IBankDao {
    List<Map<String, Object>> getBanks() throws SQLException;
}
