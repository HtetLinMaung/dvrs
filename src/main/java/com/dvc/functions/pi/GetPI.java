package com.dvc.functions.pi;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PIDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.PIDto;
import com.dvc.models.SingleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.dvc.models.MiddlewareData;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetPI {
    /**
     * This function listens at endpoint "/api/GetPI". Two ways to invoke it using
     * "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/GetPI 2. curl
     * {your host}/api/GetPI?name=HTTP%20Query
     */
    @FunctionName("getpi")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            PIDto dto = new ObjectMapper().readValue(request.getBody().get(), PIDto.class);
            PIDao dao = new PIDao();
            if (auth.getTokenData().getRole().equals("Partner")
                    && !dao.isOwnPi(dto.getSyskey(), auth.getTokenData().getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            Map<String, Object> data = new PIDao().getPi(Long.parseLong(dto.getSyskey()));
            data.put("balance", String.valueOf(
                    Integer.parseInt((String) data.get("voidcount")) + Integer.parseInt((String) data.get("balance"))));
            SingleResponse<Map<String, Object>> resData = new SingleResponse<>();
            resData.setData(data);
            return request.createResponseBuilder(HttpStatus.OK).body(resData).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
