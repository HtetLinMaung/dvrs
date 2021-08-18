package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.dvc.models.FilterDto;
import com.dvc.models.PIDto;
import com.dvc.models.PaginationResponse;

public interface IPIDao {
    PaginationResponse<Map<String, Object>> getPis(FilterDto dto) throws SQLException, IOException;

    Map<String, Object> getPi(long syskey) throws SQLException, IOException;

    Map<String, Object> addPI(PIDto dto) throws SQLException, IOException;

    int updatePI(PIDto dto) throws SQLException, IOException;

    Map<String, Object> getPIByBatch(long batchsyskey) throws SQLException, IOException;

    String getPartnerEmail(long syskey) throws SQLException;

}