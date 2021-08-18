package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.RecipientsDto;

public interface IRecipientsDao {
    PaginationResponse<Map<String, Object>> getRecipients(FilterDto dto) throws SQLException;

    Map<String, Object> getRecipient(long syskey) throws SQLException;

    int updateRecipient(RecipientsDto dto) throws SQLException, IOException;

    int deleteRecipient(RecipientsDto dto) throws SQLException;

    long addRecipient(RecipientsDto dto) throws SQLException, IOException;

    int changeRegistrationsStatus(long batchSyskey, int status) throws SQLException;

    boolean isAvailable(String fieldname, Object value) throws SQLException;

    List<Map<String, Object>> getRegisteredRecipients(long pisyskey) throws SQLException;

    int voidRecipient(long syskey, int status) throws SQLException;

    boolean isOwnRecipient(String syskey, String partnersyskey) throws SQLException;
}
