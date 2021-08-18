package com.dvc.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dvc.models.BatchDto;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.storage.StorageException;

public interface IBatchUploadDao {
    long saveBatch(BatchDto dto) throws SQLException, IOException;

    int updateBatchStatus(long syskey, int status) throws SQLException;

    int getBatchStatus(long syskey) throws SQLException;

    int updateBatchDetail(Map<String, Object> args) throws SQLException;

    List<Map<String, Object>> getBatchDetailsByHeader(long syskey) throws SQLException;

    void saveRecipents(BatchDto dto, ExecutionContext context) throws SQLException, IOException;

    List<Map<String, Object>> getBatchDataList(long syskey) throws SQLException, IOException;

    PaginationResponse<Map<String, Object>> getBatchList(FilterDto dto) throws SQLException;

    PaginationResponse<Map<String, Object>> getBatchDetails(FilterDto dto) throws SQLException;

    boolean isBatchRefCodeAvailable(String batchRefCode) throws SQLException;

    Map<String, Object> getBatch(long syskey)
            throws SQLException, IOException, InvalidKeyException, URISyntaxException, StorageException;

    int updateBatch(Map<String, Object> args) throws SQLException;

    boolean isBatchNoValid(String batchno) throws SQLException;
}
