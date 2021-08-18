package com.dvc.functions.postupload;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.PostUploadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PostAttachmentDto;
import com.dvc.utils.EasyData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UploadPostAttachments {
    /**
     * This function listens at endpoint "/api/UploadPostAttachments". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UploadPostAttachments 2. curl {your
     * host}/api/UploadPostAttachments?name=HTTP%20Query
     */
    @FunctionName("uploadpostattachments")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            PostAttachmentDto dto = new ObjectMapper().readValue(request.getBody().get(), PostAttachmentDto.class);
            List<Map<String, Object>> datalist = new PostUploadDao().uploadPost(dto);

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
