package com.dvc.functions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class QueneTest {
    /**
     * This function listens at endpoint "/api/QueneTest". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/QueneTest 2. curl {your host}/api/QueneTest?name=HTTP%20Query
     */
    @FunctionName("QueneTest")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @QueueOutput(name = "message", queueName = "myquene", connection = "AzureStorageConnectionString") OutputBinding<String> message,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        message.setValue(request.getQueryParameters().get("name"));
        return request.createResponseBuilder(HttpStatus.OK).body("Done").build();
    }
}
