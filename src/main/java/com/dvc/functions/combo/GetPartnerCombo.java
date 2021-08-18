package com.dvc.functions.combo;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.EasyData;
import com.dvc.utils.EasySql;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetPartnerCombo {
    /**
     * This function listens at endpoint "/api/GetPartnerCombo". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetPartnerCombo 2. curl {your
     * host}/api/GetPartnerCombo?name=HTTP%20Query
     */
    @FunctionName("getpartnercombo")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            List<Map<String, Object>> datalist = new EasySql(DbFactory.getConnection())
                    .getMany(Arrays.asList("syskey", "partnername", "partnerid"), "Partners where recordstatus <> 4");

            Map<String, Object> map = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            map.put("datalist", datalist);
            return request.createResponseBuilder(HttpStatus.OK).body(map).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
