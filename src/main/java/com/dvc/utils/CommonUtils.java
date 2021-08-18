package com.dvc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.models.PdfQueueMessage;
import com.dvc.models.RecipentsData;
import com.dvc.models.SingleResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;

public class CommonUtils {
    public static ByteArrayInputStream getInputStreamFromBase64(String base64File) {
        String file = base64File;
        if (base64File.split(",").length > 1) {
            file = base64File.split(",")[1];
        }
        byte[] decbytes = Base64.getDecoder().decode(file);
        return new ByteArrayInputStream(decbytes);
    }

    public static byte[] getByteArrayFromInputStream(InputStream is) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    public static byte[] getByteArrayFromBase64File(String file) throws IOException {

        return getByteArrayFromInputStream(getInputStreamFromBase64(file));
    }

    public static byte[] convertFileToByteArray(File file) {
        FileInputStream fis = null;
        // Creating bytearray of same length as file
        byte[] bArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            // Reading file content to byte array
            fis.read(bArray);
            fis.close();
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return bArray;
    }

    public static String checkPhone(String ph) {
        String phone = ph;
        if (PhoneValidation.isPhone(phone)) {
            if (PhoneValidation.validatePhone(phone)) {
                if (!PhoneValidation.check(phone)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("+959");
                    if (phone.substring(0, 2).equals("09")) {
                        sb.append(phone.substring(2));
                    } else if (phone.substring(0, 3).equals("959")) {
                        sb.append(phone.substring(3));
                    } else {
                        sb.append(phone);
                    }
                    phone = (sb.toString());
                    return phone;
                } else {
                    return phone;
                }
            } else {
                return phone;
            }
        } else {
            return phone;
        }
    }

    public HttpResponseMessage writeFiles(HttpRequestMessage<Optional<String>> request,
            List<OutputBinding<String>> queueMsgList, final String batchNo, SingleResponse<Map<String, Object>> resData,
            String isOverride, boolean isRegeneratedFiles) {
        long batch = 0l;

        try {
            batch = Long.parseLong(batchNo);
        } catch (NumberFormatException e) {
            batch = 0l;
            e.printStackTrace();
            resData.setRetcode(ServerStatus.INVALID_REQUEST);
            resData.setRetmessage(ServerMessage.INVALID_REQUEST);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(resData).build();
        }
        long partnerSyskey = 0l;
        boolean isAdmin = false;
        String btoken = request.getHeaders().get("authorization");
        try {
            if (btoken != null) {
                if (btoken.contains("Bearer")) {
                    btoken = btoken.replace("Bearer ", "");
                }
                if (isRegeneratedFiles) {
                    isAdmin = ServerUtil.isBTokenAuthNotPartner(btoken);
                } else {
                    isAdmin = ServerUtil.isBTokenAuthAdmin(btoken);
                }
                partnerSyskey = Long.parseLong(ServerUtil.getPartnerSkFromBToken(btoken));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv(PDFUtil.BLOB_CONNECTION_ENV_NAME)).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(PDFUtil.BLOB_CONTAINER_NAME);
        int totalCount = new RecipientsDownloadDao().getTotalRecordsByBatchUploadSyskey(batch);
        double numberOfTriggers = Math.floor((double) totalCount / PDFUtil.MAX_RECORDS_PER_PDF);
        numberOfTriggers += (double) totalCount % PDFUtil.MAX_RECORDS_PER_PDF > 0 ? 1 : 0;
        new RecipientsDownloadDao().updateTotalFilesBatchUpload(batch, numberOfTriggers * 2, (double) totalCount);

        ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
        rList = new RecipientsDownloadDao().getRecipentsListByBatch(batch, 1, 0, partnerSyskey, isAdmin, false);
        String folderName = new PDFUtil().generateFolderName(rList);
        final String blobFolderName = folderName;
        List<String> existedPDFList = new ArrayList<>();
        existedPDFList = new PDFUtil().getExistedPDFNameList(containerClient, blobFolderName);
        if (existedPDFList.size() > PDFUtil.INDEX_ZERO) {
            existedPDFList.forEach(e -> containerClient.getBlobClient(e).delete());
        }

        new RecipientsDownloadDao().updateStartAndEndTime(batch, true);

        PdfQueueMessage message = new PdfQueueMessage();
        if (totalCount > 0) {
            if (totalCount <= PDFUtil.MAX_RECORDS_PER_PDF) {
                message.setOffset(0);
                message.setBatchNo(batch);
                message.setOverride(isOverride.equals("1") ? true : false);
                message.setPartnerSyskey(partnerSyskey);
                message.setAdmin(isAdmin);
                // message.setContext(context);
                if (queueMsgList.size() > 0) {
                    String jsonString = new Gson().toJson(message);
                    queueMsgList.get(PDFUtil.INDEX_ONE).setValue(jsonString);
                }
            } else if (totalCount <= PDFUtil.MAX_WRITABLE_TOTAL_RECORDS) {
                int msgIndex = 0;
                for (int index = PDFUtil.INDEX_ZERO; index < totalCount; index += PDFUtil.MAX_RECORDS_PER_PDF) {
                    message = new PdfQueueMessage();
                    message.setOffset(index);
                    message.setBatchNo(batch);
                    message.setOverride(isOverride.equals("1") ? true : false);
                    message.setPartnerSyskey(partnerSyskey);
                    message.setAdmin(isAdmin);
                    // message.setContext(context);
                    if (msgIndex <= (queueMsgList.size() - 1)) {
                        String jsonString = new Gson().toJson(message);
                        queueMsgList.get(msgIndex).setValue(jsonString);
                    }
                    msgIndex++;
                }
            }
            resData.setRetcode(ServerStatus.SUCCESS);
            resData.setRetmessage(ServerMessage.SUCCESS);
            return request.createResponseBuilder(HttpStatus.OK).body(resData).build();
        } else {
            resData.setRetcode(ServerStatus.NO_DATA_ERROR);
            resData.setRetmessage(ServerMessage.NO_DATA_ERROR);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(resData).build();
        }
    }

    public static void writeFiles(String batchno) {

        final String batchNo = batchno;
        /*
         * final String overrideFlag = request.getQueryParameters().get("code"); final
         * String overrideCode = request.getBody().orElse(overrideFlag);
         */
        // SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
        String folderName = "noFile";

        if (batchNo == null) {
            // context.getLogger().warning("batch no null!");
            // resData.setRetcode(ServerStatus.INVALID_REQUEST);
            // resData.setRetmessage(ServerMessage.INVALID_REQUEST);
            // return
            // request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            /*
             * } else if (overrideCode == null) {
             * context.getLogger().warning("code no null!");
             * resData.setRetcode(ServerStatus.INVALID_REQUEST);
             * resData.setRetmessage(ServerMessage.INVALID_REQUEST); return
             * request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
             */
        } else {
            long batch = 0l;
            boolean isOverride = true;

            try {
                batch = Long.parseLong(batchNo);
                // isOverride = Integer.parseInt(overrideCode) == 1 ? true : false;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
            rList = new RecipientsDownloadDao().getRecipentsListByBatch(batch, 0, 0, 0l, true, true);

            if (rList.size() > PDFUtil.INDEX_ZERO) {
                List<String> existedPDFList = new ArrayList<>();
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(System.getenv(PDFUtil.BLOB_CONNECTION_ENV_NAME)).buildClient();
                BlobContainerClient containerClient = blobServiceClient
                        .getBlobContainerClient(PDFUtil.BLOB_CONTAINER_NAME);

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
                    // resData.setRetcode(ServerStatus.SUCCESS);
                    // resData.setRetmessage(ServerMessage.SUCCESS);
                    // return request.createResponseBuilder(HttpStatus.OK).body(resData).build();
                } else {
                    // resData.setRetcode(ServerStatus.INVALID_REQUEST);
                    // resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                    // return
                    // request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
                }
            } else {
                // context.getLogger().warning("no data was found");
                // resData.setRetcode(ServerStatus.INVALID_REQUEST);
                // resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                // return
                // request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            }
        }
    }
}
