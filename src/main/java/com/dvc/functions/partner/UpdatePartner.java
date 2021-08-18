package com.dvc.functions.partner;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PartnerDao;
import com.dvc.dao.UserHistoryDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PartnerDto;
import com.dvc.models.UserHistoryDto;
import com.dvc.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UpdatePartner {
    /**
     * This function listens at endpoint "/api/UpdatePartner". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UpdatePartner 2. curl {your
     * host}/api/UpdatePartner?name=HTTP%20Query
     */
    @FunctionName("updatepartner")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
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
            PartnerDto dto = new ObjectMapper().readValue(request.getBody().get(), PartnerDto.class);
            String phone = CommonUtils.checkPhone(dto.getContactnumber());
            dto.setContactnumber(phone);
            UserHistoryDto userHistoryDto = new UserHistoryDto();
            userHistoryDto.setType(1);
            userHistoryDto.setPartnerDto(dto);
            userHistoryDto.setIpaddress(request.getHeaders().get("x-forwarded-for"));
            UserHistoryDao.getInstance().addUserHistory(userHistoryDto);
            int result = new PartnerDao().updatePartner(dto);
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
