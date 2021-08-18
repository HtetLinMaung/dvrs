package com.dvc.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dvc.models.PostAttachmentDto;

public interface IPostUploadDao {
    List<Map<String, Object>> uploadPost(PostAttachmentDto dto) throws SQLException;

    List<Map<String, Object>> getAttachmentsByBatch(long batchsyskey) throws SQLException;
}
