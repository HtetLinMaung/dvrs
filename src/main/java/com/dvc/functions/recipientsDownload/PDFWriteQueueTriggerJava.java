package com.dvc.functions.recipientsDownload;

import com.microsoft.azure.functions.annotation.*;

import java.util.ArrayList;
import java.util.List;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.models.RecipentsData;
import com.dvc.utils.PDFUtil;
import com.dvc.utils.WriteExcel;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class PDFWriteQueueTriggerJava {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("PDFWriteQueueTriggerJava")
    public void run(
            @QueueTrigger(name = "message", queueName = "pdf-queue", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        long batch = 0l;
        boolean isOverride = true;
        String folderName = "noFile";
        try {
            batch = Long.parseLong(message);
            // isOverride = Integer.parseInt(overrideCode) == 1 ? true : false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
        rList = new RecipientsDownloadDao().getRecipentsListByBatch(batch, 0, 0, 0l, true, false);

        if (rList.size() > PDFUtil.INDEX_ZERO) {
            List<String> existedPDFList = new ArrayList<>();
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(System.getenv(PDFUtil.BLOB_CONNECTION_ENV_NAME)).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(PDFUtil.BLOB_CONTAINER_NAME);

            folderName = new PDFUtil().generateFolderName(rList);
            final String blobFolderName = folderName;

            existedPDFList = new PDFUtil().getExistedPDFNameList(containerClient, blobFolderName);
            byte[] excelArr = new WriteExcel().recipientsToExcel(rList);
            new PDFUtil().writeFileAtBlobStorage(folderName + PDFUtil.EXCEL_EXTENSION, excelArr, folderName,
                    PDFUtil.EXCEL_CONTENT_TYPE, containerClient);

            new PDFUtil().checkAndWritePDF(folderName, rList, isOverride, existedPDFList, containerClient);

            if (existedPDFList.size() > PDFUtil.INDEX_ZERO) {
                existedPDFList.forEach(e -> containerClient.getBlobClient(e).delete());
            }

            existedPDFList = new PDFUtil().getExistedPDFNameList(containerClient, blobFolderName);
            if (existedPDFList.size() > 0) {
                new RecipientsDownloadDao().updateBatchUploadStatus(batch);
            }
        }
    }
}
