package com.dvc.functions.qr;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.models.BaseResponse;
import com.dvc.models.SingleResponse;
import com.dvc.utils.JwtUtils;
import com.dvc.utils.QRNewUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetYQR {
    /**
     * This function listens at endpoint "/api/GetYQR". Two ways to invoke it using
     * "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/GetYQR 2. curl
     * {your host}/api/GetYQR?name=HTTP%20Query
     */
    @FunctionName("getyqr")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            Map<String, String> dto = new ObjectMapper().readValue(request.getBody().get(), Map.class);
            String result = QRNewUtils.decryptQRToken(dto.get("qrtoken"));

            SingleResponse<String> res = new SingleResponse<>();
            res.setData(result);
            return request.createResponseBuilder(HttpStatus.OK).body(res).build();
        } catch (Exception e) {
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
