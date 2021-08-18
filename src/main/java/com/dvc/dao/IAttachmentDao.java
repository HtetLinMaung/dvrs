package com.dvc.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dvc.models.AttachmentDto;

public interface IAttachmentDao {
    List<Map<String, Object>> uploadAttachments(AttachmentDto dto) throws SQLException;

    Map<String, Object> getAttachment(long syskey) throws SQLException;

    List<Map<String, Object>> getAttachmentsByPi(long pisyskey) throws SQLException;

    boolean isOwnAttachment(long syskey, long partnersyskey) throws SQLException;
}
