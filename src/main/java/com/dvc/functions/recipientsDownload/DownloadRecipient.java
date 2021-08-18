package com.dvc.functions.recipientsDownload;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
import com.itextpdf.text.DocumentException;
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
public class DownloadRecipient {
    /**
     * This function listens at endpoint "/api/downloadPDF". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/downloadZip 2. curl "{your
     * host}/api/downloadZip?batchno=HTTP%20Query"
     */
    @FunctionName("downloadPDF")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("downloadPDF trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("id");
        String syskey = request.getBody().orElse(query);
        final String btokenQuery = request.getQueryParameters().get("token");
        String btoken = request.getBody().orElse(btokenQuery);
        SingleResponse<Map<String, Object>> resData = new SingleResponse<>();

        MiddlewareData auth = SecurityFileDownloadMiddleware.checkAuthorization(request);
        MiddlewareData adminOrPartnerAuth = SecurityFileDownloadMiddleware.checkIFAdminOrPartnerAuthorization(request);
        if (!auth.isSuccess()) {
            return auth.getResponse();
        } else if (!adminOrPartnerAuth.isSuccess()) {
            return adminOrPartnerAuth.getResponse();
        }
        if (syskey == null) {
            context.getLogger().warning("syskey null!");
            resData.setRetcode(ServerStatus.INVALID_REQUEST);
            resData.setRetmessage(ServerMessage.INVALID_REQUEST);
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
        } else {
            try {
                long recipientSyskey = 0l;
                long partnerSyskey = 0l;
                boolean isAdmin = false;

                String encodedURI = syskey.replace(" ", "+");
                syskey = AESAlgorithm.decryptString(encodedURI);
                if (syskey == null) {
                    syskey = "0";
                }
                try {
                    recipientSyskey = Long.parseLong(syskey);
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
                RecipentsData recipientsData = new RecipentsData();

                recipientsData = new RecipientsDownloadDao().getRecipentBySyskeyOrCID(recipientSyskey, true, "0",
                        partnerSyskey, isAdmin);
                byte[] pdfByte = new PDFUtil().writePdf(Arrays.asList(recipientsData));
                String pdfName = URLEncoder.encode(recipientsData.getPdfName() + PDFUtil.PDF_EXTENSION, "UTF-8");
                pdfName = pdfName.replaceAll("\\+", " ");
                context.getLogger()
                        .info(String.format("Retrieving data was successed with recipient syskey : %s", syskey));
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Disposition", "attachment; filename=" + pdfName).body(pdfByte).build();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.getLogger().warning("no data was found");
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Disposition", "attachment; filename=" + "nodata" + PDFUtil.PDF_EXTENSION)
                    .body(new byte[PDFUtil.INDEX_ZERO]).build();

        }
    }

}
