package com.dvc.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dvc.models.PostAttachmentDto;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.KeyGenerator;
import com.microsoft.azure.storage.StorageException;

public class PostUploadDao extends BaseDao implements IPostUploadDao {

    @Override
    public List<Map<String, Object>> uploadPost(PostAttachmentDto dto) throws SQLException {
        final String now = Instant.now().toString();

        List<Map<String, Object>> datalist = dto.getFiles().stream().map(fileData -> {
            Map<String, Object> m = new HashMap<>();
            String filename = UUID.randomUUID().toString() + "-" + fileData.getFilename();
            m.put("syskey", KeyGenerator.generateSyskey());
            m.put("createddate", now);
            m.put("modifieddate", now);
            m.put("filename", filename);
            m.put("description", fileData.getDescription());
            m.put("batchuploadsyskey", dto.getBatchsyskey());
            try {
                AzureBlobUtils.createBlob(fileData.getFile(), filename);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return m;
        }).collect(Collectors.toList());

        getDBClient().insertMany("PostAttachmentFiles", datalist);

        return datalist.stream().map(data -> {
            Map<String, Object> m = new HashMap<>();
            // m.put("url", "https://apx.registrationsystem.org/api/downloadattachment?id="
            // + data.get("syskey"));

            try {
                m.put("url", AzureBlobUtils.getBlobClient((String) data.get("filename")).getBlobUrl() + "?"
                        + AzureBlobUtils.getSasToken());
            } catch (InvalidKeyException | URISyntaxException | StorageException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            m.put("syskey", data.get("syskey"));
            m.put("filename", data.get("filename"));
            m.put("description", data.get("description"));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAttachmentsByBatch(long batchsyskey) throws SQLException {

        return getDBClient().getMany(Arrays.asList("syskey", "filename", "description"),
                "PostAttachmentFiles where batchuploadsyskey = ?", Arrays.asList(batchsyskey));
    }

}
