package com.dvc.functions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.models.BaseResponse;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetConfigurations {
    /**
     * This function listens at endpoint "/api/GetConfigurations". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetConfigurations 2. curl {your
     * host}/api/GetConfigurations?name=HTTP%20Query
     */
    @FunctionName("getconfigurations")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            Map<String, Object> configs = new HashMap<>();
            configs.put("apptitle", System.getenv("APP_TITLE"));
            configs.put("appversion", System.getenv("APP_VERSION"));
            configs.put("primarycolor", System.getenv("PRIMARY_COLOR"));
            configs.put("secondarycolor", System.getenv("SECONDARY_COLOR"));
            return request.createResponseBuilder(HttpStatus.OK).body(configs).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
