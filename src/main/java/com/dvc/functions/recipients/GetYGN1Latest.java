package com.dvc.functions.recipients;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.EasyData;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetYGN1Latest {
    /**
     * This function listens at endpoint "/api/GetYGN1Latest". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetYGN1Latest 2. curl {your
     * host}/api/GetYGN1Latest?name=HTTP%20Query
     */
    @FunctionName("getygn1latest")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            List<Map<String, Object>> datalist = new RecipientsDao().getYgn1Latest();
            if (datalist.size() == 0) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.NO_DATA_ERROR);
                res.setRetmessage(ServerMessage.NO_DATA_ERROR);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(res).build();
            }
            SingleResponse<Map<String, Object>> res = new SingleResponse<>();
            res.setData(datalist.get(0));

            // Map<String, Object> res = new EasyData<BaseResponse>(new
            // BaseResponse()).toMap();
            // res.put("datalist", datalist);
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
