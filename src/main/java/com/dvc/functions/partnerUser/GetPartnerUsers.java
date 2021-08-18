package com.dvc.functions.partnerUser;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PartnerUserDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.FilterDto;
import com.dvc.models.MiddlewareData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dvc.models.PaginationResponse;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetPartnerUsers {
    /**
     * This function listens at endpoint "/api/GetPartnerUsers". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GetPartnerUsers 2. curl {your
     * host}/api/GetPartnerUsers?name=HTTP%20Query
     */
    @FunctionName("getpartnerusers")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "partneruserlist") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

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
            FilterDto dto = new ObjectMapper().readValue(request.getBody().get(), FilterDto.class);
            PaginationResponse<Map<String, Object>> resData = new PartnerUserDao().getPartnerUsers(dto);
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
