package com.dvc.functions.batchupload;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.CommonConstants;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.ComboData;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.EasyData;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetBatchStatusList {
    /**
     * This function listens at endpoint "/api/GetBatchStatusList". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetBatchStatusList 2. curl {your
     * host}/api/GetBatchStatusList?name=HTTP%20Query
     */
    @FunctionName("getbatchstatuslist")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            EasyData<BaseResponse> easyData = new EasyData<>(new BaseResponse());
            Map<String, Object> res = easyData.toMap();

            List<ComboData> approvallist = new ArrayList<>();
            if (Arrays.asList("Admin", "Finance").contains(auth.getTokenData().getRole())) {
                approvallist = Arrays.asList(

                        new ComboData("Approve", 30)

                );
            } else if (auth.getTokenData().getRole().equals("Partner")) {
                approvallist = Arrays.asList(

                        new ComboData("Submit", 20)

                );
            }
            res.put("approvallist", approvallist);
            res.put("paymentlist", CommonConstants.PAYMENT_LIST);
            res.put("voidlist", CommonConstants.VOID_LIST);
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
