package com.dvc.functions.pdfqueue;

import com.microsoft.azure.functions.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.models.PdfQueueMessage;
import com.dvc.models.RecipentsData;
import com.dvc.utils.PDFUtil;
import com.dvc.utils.WriteExcel;
import com.google.gson.Gson;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class PDFWriterQueueTriggerTenJava {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("PDFWriterQueueTriggerTenJava")
    public void run(
            @QueueTrigger(name = "messageTen", queueName = "pdf-queue-ten", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        PdfQueueMessage pdfMessage = new Gson().fromJson(message, PdfQueueMessage.class);

        String folderName = "noFile";

        ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
        rList = new RecipientsDownloadDao().getRecipentsListByBatch(pdfMessage.getBatchNo(), 0, pdfMessage.getOffset(),
                pdfMessage.getPartnerSyskey(), pdfMessage.isAdmin(), false);

        if (rList.size() > PDFUtil.INDEX_ZERO) {
            List<String> existedPDFList = new ArrayList<>();
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(System.getenv(PDFUtil.BLOB_CONNECTION_ENV_NAME)).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(PDFUtil.BLOB_CONTAINER_NAME);

            folderName = new PDFUtil().generateFolderName(rList);
            final String blobFolderName = folderName;

            existedPDFList = new PDFUtil().getExistedPDFNameList(containerClient, blobFolderName);
            byte[] excelArr = new WriteExcel().recipientsToExcel(rList);
            new PDFUtil().writeFileAtBlobStorage(
                    new PDFUtil().generateFolderNameForBatch(rList) + PDFUtil.EXCEL_EXTENSION, excelArr, folderName,
                    PDFUtil.EXCEL_CONTENT_TYPE, containerClient);

            new PDFUtil().checkAndWritePDF(folderName, rList, pdfMessage.isOverride(), existedPDFList, containerClient);

            existedPDFList = new ArrayList<>();
            existedPDFList = new PDFUtil().getExistedPDFNameList(containerClient, blobFolderName);
            new RecipientsDownloadDao().updateRecordStatusBatchUpload(pdfMessage.getBatchNo(), existedPDFList.size(),
                    folderName, context);
        }
    }
}
