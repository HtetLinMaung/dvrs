package com.dvc.functions.batchupload.quene;

import com.microsoft.azure.functions.annotation.*;

import java.util.List;
import java.util.Map;

import com.dvc.dao.BatchUploadDao;

import com.dvc.models.BatchDto;
import com.dvc.utils.CommonUtils;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QSaveRecipients {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("QSaveRecipients")
    public void run(
            @QueueTrigger(name = "message", queueName = "save-recipients", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        try {
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(message));

            List<Map<String, Object>> validdatalist = new BatchUploadDao()
                    .getBatchDetailsByHeader(Long.parseLong(message), 1);
            BatchDto dtoData = new BatchDto();
            dtoData.setDatalist(validdatalist);
            dtoData.setBatchsyskey(message);
            dtoData.setPisyskey((String) batch.get("pisyskey"));
            dtoData.setCenterid((String) batch.get("centerid"));
            dtoData.setPartnersyskey((String) batch.get("partnersyskey"));
            if (batch.get("recipientsaved").equals("0")) {
                new BatchUploadDao().saveRecipents(dtoData, context);
                ;
            }
            CommonUtils.writeFiles(message);
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
        }
    }
}
