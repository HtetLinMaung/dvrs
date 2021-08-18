package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dvc.models.CenterDto;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;

public interface ICenterDao {
    List<Map<String, Object>> getCenters() throws SQLException;

    List<String> addLastSerial(String centerid, int count) throws SQLException;

    int getRemainingCount(long pisyskey) throws SQLException, IOException;

    Map<String, Object> getCenter(String centerid) throws SQLException;

    PaginationResponse<Map<String, Object>> getCenters(FilterDto dto) throws SQLException;

    long addCenter(CenterDto dto) throws SQLException, IOException;

    int updateCenter(CenterDto dto) throws SQLException, IOException;

    int deleteCenter(CenterDto dto) throws SQLException;
}
