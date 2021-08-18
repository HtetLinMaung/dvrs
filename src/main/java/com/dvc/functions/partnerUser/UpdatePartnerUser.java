package com.dvc.functions.partnerUser;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PartnerUserDao;
import com.dvc.dao.UserHistoryDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PartnerUserDto;
import com.dvc.models.UserHistoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UpdatePartnerUser {
    /**
     * This function listens at endpoint "/api/UpdatePartnerUser". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UpdatePartnerUser 2. curl {your
     * host}/api/UpdatePartnerUser?name=HTTP%20Query
     */
    @FunctionName("updatepartneruser")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "partneruser") HttpRequestMessage<Optional<String>> request,
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
            PartnerUserDto dto = new ObjectMapper().readValue(request.getBody().get(), PartnerUserDto.class);
            UserHistoryDto userHistoryDto = new UserHistoryDto();
            userHistoryDto.setType(2);
            userHistoryDto.setPartnerUserDto(dto);
            userHistoryDto.setIpaddress(request.getHeaders().get("x-forwarded-for"));
            UserHistoryDao.getInstance().addUserHistory(userHistoryDto);
            int result = new PartnerUserDao().updatePartnerUser(dto);
            BaseResponse resData = new BaseResponse();
            if (result == 0) {
                resData.setRetcode(ServerStatus.INVALID_REQUEST);
                resData.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).header("Content-Type", "application/json")
                        .body(resData).build();
            }

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
