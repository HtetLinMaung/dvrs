package com.dvc.functions;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.storage.StorageException;
import com.dvc.utils.AzureBlobUtils;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetExcel {
    /**
     * This function listens at endpoint "/api/GetExcel". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/GetExcel
     * 2. curl {your host}/api/GetExcel?name=HTTP%20Query
     */
    @FunctionName("GetExcel")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);
        String url = "";
        try {
            url = "https://dvrs.blob.core.windows.net/dvrsuploads/d82de11d-47f7-4fd9-9f6f-e3615e44d024_PC%20Myanmar%20%283?"

                    + AzureBlobUtils.getSasToken();
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(url).build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(url).build();
        }
    }
}
