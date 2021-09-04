package com.dvc.functions.pi;

import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PIDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.FilterDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PaginationResponse;
import com.dvc.utils.EasyData;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class GetPis {
    /**
     * This function listens at endpoint "/api/GetPis". Two ways to invoke it using
     * "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/GetPis 2. curl
     * {your host}/api/GetPis?name=HTTP%20Query
     */
    @FunctionName("getpis")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            FilterDto dto = new ObjectMapper().readValue(request.getBody().get(), FilterDto.class);
            dto.setRole(auth.getTokenData().getRole());
            String partnersyskey = auth.getTokenData().getPartnersyskey();
            if (auth.getTokenData().getRole().equals("Partner")) {
                dto.setPartnersyskey(partnersyskey);
            }

            PaginationResponse<Map<String, Object>> resData = new PIDao().getPis(dto);
            Map<String, Object> totalData = new PIDao().getTotalQB(dto.getPartnersyskey(), dto.getCenterid());
            Map<String, Object> body = new EasyData<PaginationResponse<Map<String, Object>>>(resData).toMap();
            body.putAll(totalData);
            return request.createResponseBuilder(HttpStatus.OK).body(body).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
