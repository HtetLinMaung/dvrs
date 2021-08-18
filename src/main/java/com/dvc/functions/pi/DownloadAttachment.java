package com.dvc.functions.pi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.dao.AttachmentDao;
import com.dvc.utils.AzureBlobUtils;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class DownloadAttachment {
    /**
     * This function listens at endpoint "/api/DownloadAttachment". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/DownloadAttachment 2. curl {your
     * host}/api/DownloadAttachment?name=HTTP%20Query
     */
    @FunctionName("downloadattachment")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            final String syskey = request.getQueryParameters().get("id");

            Map<String, Object> attachment = new AttachmentDao().getAttachment(Long.parseLong(syskey));

            String url = AzureBlobUtils.getBlobClient((String) attachment.get("filename")).getBlobUrl() + "?"
                    + AzureBlobUtils.getSasToken();
            InputStream in = new URL(url).openStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Disposition",
                            String.format("attachment;filename=\"%s.png\"", attachment.get("filename")))
                    .body(buffer.toByteArray()).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
        }
    }
}
