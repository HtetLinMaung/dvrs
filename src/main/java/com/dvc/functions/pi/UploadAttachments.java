package com.dvc.functions.pi;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.AttachmentDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.AttachmentDto;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.EasyData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UploadAttachments {
    /**
     * This function listens at endpoint "/api/UploadAttachments". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UploadAttachments 2. curl {your
     * host}/api/UploadAttachments?name=HTTP%20Query
     */
    @FunctionName("uploadattachments")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            AttachmentDto dto = new ObjectMapper().readValue(request.getBody().get(), AttachmentDto.class);
            List<Map<String, Object>> datalist = new AttachmentDao().uploadAttachments(dto);

            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("attachmentlist", datalist);
            res.put("retmessage", "Uploaded Successfully");
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
