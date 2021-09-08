package com.dvc.functions.recipients;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.FilterDto;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.AzureBlobUtils;
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
public class DownloadMohsReport {
    /**
     * This function listens at endpoint "/api/DownloadMohsReport". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/DownloadMohsReport 2. curl {your
     * host}/api/DownloadMohsReport?name=HTTP%20Query
     */
    @FunctionName("downloadmohsreport")
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
            if (dto.getRole().equals("Partner")) {
                dto.setPartnersyskey(auth.getTokenData().getPartnersyskey());
            }
            RecipientsDao dao = new RecipientsDao();
            List<Map<String, Object>> datalist = dao.getMohsExcel(dto.getGroupcode(), dto.getSubgroupcode());
            if (datalist.size() == 0) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.NO_DATA_ERROR);
                res.setRetmessage(ServerMessage.NO_DATA_ERROR);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(res).build();
            }
            
            String url = AzureBlobUtils.getBlobClient((String) datalist.get(0).get("filename")).getBlobUrl();
            url += "?" + AzureBlobUtils.getSasToken();
            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("url", url);
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
