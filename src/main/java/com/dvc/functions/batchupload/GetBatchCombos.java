package com.dvc.functions.batchupload;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.EasyData;
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
public class GetBatchCombos {
    /**
     * This function listens at endpoint "/api/GetBatchCombos". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetBatchCombos 2. curl {your
     * host}/api/GetBatchCombos?name=HTTP%20Query
     */
    @FunctionName("GetBatchCombos")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            List<Map<String, Object>> datalist = new BatchUploadDao().getBatchCombos(auth.getTokenData().getRole(),
                    auth.getTokenData().getPartnersyskey() != null ? auth.getTokenData().getPartnersyskey() : "");
            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("datalist", datalist);
            return request.createResponseBuilder(HttpStatus.OK).body(res).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
