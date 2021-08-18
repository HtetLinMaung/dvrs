package com.dvc.functions.batchupload;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.EasyData;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CheckBatchStatus {
    /**
     * This function listens at endpoint "/api/CheckBatchStatus". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/CheckBatchStatus 2. curl {your
     * host}/api/CheckBatchStatus?name=HTTP%20Query
     */
    @FunctionName("checkbatchstatus")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            final String syskey = request.getQueryParameters().get("batchsyskey");
            Map<String, Object> batch = new BatchUploadDao().getBatch(Long.parseLong(syskey));

            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("recordstatus", batch.get("recordstatus"));
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
