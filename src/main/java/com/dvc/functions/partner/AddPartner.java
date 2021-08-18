package com.dvc.functions.partner;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PartnerDao;
import com.dvc.dao.UserHistoryDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.PartnerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SaveResponse;
import com.dvc.models.UserHistoryDto;
import com.dvc.utils.CommonUtils;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AddPartner {
    /**
     * This function listens at endpoint "/api/AddPartner". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/AddPartner 2. curl {your host}/api/AddPartner?name=HTTP%20Query
     */
    @FunctionName("addpartner")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
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
            long userHistorySyskey = UserHistoryDao.getInstance().addUserHistory(userHistoryDto);

            long syskey = new PartnerDao().addPartner(dto);

            userHistoryDto.setSyskey(userHistorySyskey);
            userHistoryDto.getPartnerDto().setSyskey(String.valueOf(syskey));
            UserHistoryDao.getInstance().updateUserHistory(userHistoryDto);

            SaveResponse resData = new SaveResponse();
            resData.setSyskey(String.valueOf(syskey));
            return request.createResponseBuilder(HttpStatus.CREATED).header("Content-Type", "application/json")
                    .body(resData).build();
        } catch (Exception e) {
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json").body(res).build();
        }
    }
}
