package com.dvc.functions;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.storage.StorageException;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.Converter;
import com.dvc.utils.LanguageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class TestMyanmar {
    /**
     * This function listens at endpoint "/api/TestMyanmar". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/TestMyanmar 2. curl {your host}/api/TestMyanmar?name=HTTP%20Query
     */
    @FunctionName("TestMyanmar")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        String unicode = Converter.zg12uni51("၁၂/သဃက(နိုင်)၁၄၄၄၃၇");
        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
