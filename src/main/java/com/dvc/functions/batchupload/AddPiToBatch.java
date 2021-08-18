package com.dvc.functions.batchupload;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.BatchUploadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.TokenData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AddPiToBatch {
    /**
     * This function listens at endpoint "/api/AddPiToBatch". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/AddPiToBatch 2. curl {your host}/api/AddPiToBatch?name=HTTP%20Query
     */
    @FunctionName("addpitobatch")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            BatchDto dto = new ObjectMapper().readValue(request.getBody().get(), BatchDto.class);

            TokenData tokenData = auth.getTokenData();

            BatchUploadDao dao = new BatchUploadDao();
            if (tokenData.getRole().equals("Partner") && !dao.isBatchPaymentValid(dto.getBatchsyskey(),
                    dto.getPisyskey(), tokenData.getPartnersyskey())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            Map<String, Object> args = new HashMap<>();
            args.put("syskey", dto.getBatchsyskey());
            args.put("pisyskey", dto.getPisyskey());
            new BatchUploadDao().updateBatch(args);
            BaseResponse res = new BaseResponse();
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
