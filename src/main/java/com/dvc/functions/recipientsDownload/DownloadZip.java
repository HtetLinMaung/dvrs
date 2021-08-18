package com.dvc.functions.recipientsDownload;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.middlewares.SecurityFileDownloadMiddleware;
import com.dvc.models.MiddlewareData;
import com.dvc.models.RecipentsData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.AESAlgorithm;
import com.dvc.utils.PDFUtil;
import com.dvc.utils.ServerUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with batch download recipients with zip HTTP Trigger
 */
public class DownloadZip {
    /**
     * This function listens at endpoint "/api/downloadZip". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/downloadZip 2. curl "{your
     * host}/api/downloadZip?batchno=HTTP%2PDFUtil.INDEX_ZEROQuery"
     */
    @FunctionName("downloadZip")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("downloadZip trigger processed a request.");

        final String query = request.getQueryParameters().get("batchno");
        String batchNo = request.getBody().orElse(query);
        final String btokenQuery = request.getQueryParameters().get("token");
        String btoken = request.getBody().orElse(btokenQuery);
        SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
        String folderName = "noFile";

        MiddlewareData auth = SecurityFileDownloadMiddleware.checkAuthorization(request);
        MiddlewareData adminOrPartnerAuth = SecurityFileDownloadMiddleware.checkIFAdminOrPartnerAuthorization(request);
        if (!auth.isSuccess()) {
            return auth.getResponse();
        } else if (!adminOrPartnerAuth.isSuccess()) {
            return adminOrPartnerAuth.getResponse();
        }

        if (batchNo == null) {
            context.getLogger().warning("batch no null!");
            resData.setRetcode(ServerStatus.INVALID_REQUEST);
            resData.setRetmessage(ServerMessage.INVALID_REQUEST);
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
        } else {
            long batch = 0l;
            long partnerSyskey = 0l;
            boolean isAdmin = false;

            String encodedURI = batchNo.replace(" ", "+");
            batchNo = AESAlgorithm.decryptString(encodedURI);
            if (batchNo == null) {
                batchNo = "0";
            }
            try {
                batch = Long.parseLong(batchNo);
                if (btoken != null) {
                    if (btoken.contains("Bearer")) {
                        btoken = btoken.replace("Bearer ", "");
                    }
                    isAdmin = ServerUtil.isBTokenAuthAdmin(btoken);
                    partnerSyskey = Long.parseLong(ServerUtil.getPartnerSkFromBToken(btoken));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
            rList = new RecipientsDownloadDao().getRecipentsListByBatch(batch, 1, 0, partnerSyskey, isAdmin, true);

            if (rList.size() > PDFUtil.INDEX_ZERO) {
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(System.getenv(PDFUtil.BLOB_CONNECTION_ENV_NAME)).buildClient();
                BlobContainerClient containerClient = blobServiceClient
                        .getBlobContainerClient(PDFUtil.BLOB_CONTAINER_NAME);

                folderName = new PDFUtil().generateFolderName(rList);

                byte[] zippedData = new PDFUtil().downloadZippedByte(folderName, containerClient);

                context.getLogger().info(String.format("Downloading zip was finished with batch no : %s", batchNo));
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Disposition", "attachment; filename=" + folderName + ".zip").body(zippedData)
                        .build();
            } else {
                context.getLogger().warning("no data was found");
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Disposition", "attachment; filename=" + folderName + ".zip")
                        .body(new byte[PDFUtil.INDEX_ZERO]).build();
            }
        }
    }
}
