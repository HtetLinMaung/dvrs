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

import com.dvc.models.AttachmentDto;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.KeyGenerator;
import com.microsoft.azure.storage.StorageException;

public class AttachmentDao extends BaseDao implements IAttachmentDao {

    @Override
    public List<Map<String, Object>> uploadAttachments(AttachmentDto dto) throws SQLException {
        final String now = Instant.now().toString();

        List<Map<String, Object>> datalist = dto.getFiles().stream().map(fileData -> {
            Map<String, Object> m = new HashMap<>();
            String filename = UUID.randomUUID().toString() + "-" + fileData.getFilename();
            m.put("syskey", KeyGenerator.generateSyskey());
            m.put("createddate", now);
            m.put("modifieddate", now);
            m.put("filename", filename);
            m.put("description", fileData.getDescription());
            m.put("pisyskey", dto.getPisyskey());
            try {
                AzureBlobUtils.createBlob(fileData.getFile(), filename);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return m;
        }).collect(Collectors.toList());

        getDBClient().insertMany("AttachmentFiles", datalist);

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
    public Map<String, Object> getAttachment(long syskey) throws SQLException {
        return getDBClient().getOne(Arrays.asList("syskey", "filename", "description"),
                "AttachmentFiles where syskey = ?", Arrays.asList(syskey));
    }

    @Override
    public List<Map<String, Object>> getAttachmentsByPi(long pisyskey) throws SQLException {
        return getDBClient().getMany(Arrays.asList("syskey", "filename", "description"),
                "AttachmentFiles where pisyskey = ?", Arrays.asList(pisyskey));
    }

    @Override
    public boolean isOwnAttachment(long syskey, long partnersyskey) throws SQLException {
        int total = getTotalCount(
                "AttachmentFiles as a left join ProformaInvoice as p on a.pisyskey = p.syskey where a.syskey = ? and p.partnersyskey = ?",
                Arrays.asList(syskey, partnersyskey));
        if (total == 0) {
            return false;
        }
        return true;
    }

}