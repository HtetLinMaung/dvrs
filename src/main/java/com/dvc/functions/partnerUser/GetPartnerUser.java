package com.dvc.functions.partnerUser;

import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PartnerUserDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.AESAlgorithm;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetPartnerUser {
    /**
     * This function listens at endpoint "/api/GetPartnerUser". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetPartnerUser 2. curl {your
     * host}/api/GetPartnerUser?name=HTTP%20Query
     */
    @FunctionName("getpartneruser")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "partneruser") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        String query = request.getQueryParameters().get("id");
        String syskey = request.getBody().orElse(query);
        SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            } else {
                MiddlewareData authAdmin = SecurityMiddleware.checkAuthAdmin(request);
                if (!authAdmin.isSuccess()) {
                    return authAdmin.getResponse();
                }
            }

            String encodedURI = syskey.replace(" ", "+");
            syskey = AESAlgorithm.decryptString(encodedURI);
            if (syskey == null) {
                syskey = "0";
            }
            Map<String, Object> data = new PartnerUserDao().getPartnerUser(Long.parseLong(syskey));
            if (((String) data.get("syskey")).isEmpty()) {
                resData.setRetcode(ServerStatus.INVALID_REQUEST);
                resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).header("Content-Type", "application/json")
                        .body(resData).build();
            }
            resData.setData(data);
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(resData)
                    .build();
        } catch (Exception e) {
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json").body(res).build();
        }
    }
}
