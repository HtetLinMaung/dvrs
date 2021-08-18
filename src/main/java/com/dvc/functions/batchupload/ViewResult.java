package com.dvc.functions.batchupload;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.dao.BatchUploadDao;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;
import com.dvc.utils.ValidateBatchUtils;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ViewResult {
    /**
     * This function listens at endpoint "/api/ViewResult". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ViewResult 2. curl {your host}/api/ViewResult?name=HTTP%20Query
     */
    @FunctionName("viewresult")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            final String syskey = request.getQueryParameters().get("id");
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(syskey));
            String batchno = ((String) batch.get("batchrefcode")).split("-")[1];

            String url = "https://dvrs.blob.core.windows.net/dvrsuploads/" + batchno + "_invalid.txt";
            InputStream in = new URL(url).openStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Disposition", String.format("inline;filename=\"%s_invalid.txt\"", batchno))
                    .body(buffer.toByteArray()).build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Disposition", "inline;filename=\"BXXX_invalid.txt\"").body("Something went wrong!")
                    .build();
        }
    }
}
