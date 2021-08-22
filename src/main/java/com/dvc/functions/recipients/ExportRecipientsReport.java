package com.dvc.functions.recipients;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.ReportDto;
import com.dvc.models.TokenData;
import com.dvc.utils.ExcelUtil;
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
public class ExportRecipientsReport {
    /**
     * This function listens at endpoint "/api/ExportRecipientsReport". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ExportRecipientsReport 2. curl {your
     * host}/api/ExportRecipientsReport?name=HTTP%20Query
     */
    @FunctionName("exportrecipientsreport")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            TokenData tokenData = auth.getTokenData();
            ReportDto dto = new ObjectMapper().readValue(request.getBody().get(), ReportDto.class);
            if (tokenData.getRole().equals("Partner")) {
                dto.setPartnersyskey(tokenData.getPartnersyskey());
            }
            List<Map<String, Object>> datalist = new RecipientsDao().getRecipients(dto);
            byte[] byteArr = ExcelUtil.writeExcel(datalist, "Sheet1");
            return request
                    .createResponseBuilder(HttpStatus.OK).header("Content-Disposition", String
                            .format("attachment;filename=\"%s.xls\"", UUID.randomUUID().toString() + "-" + "report"))
                    .body(byteArr).build();

        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
