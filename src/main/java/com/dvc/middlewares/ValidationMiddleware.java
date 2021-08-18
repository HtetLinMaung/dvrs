package com.dvc.middlewares;

import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;

public class ValidationMiddleware {
    public static MiddlewareData checkValidation(HttpRequestMessage<Optional<String>> request) {
        MiddlewareData middlewareData = new MiddlewareData();
        BaseResponse res = new BaseResponse();
        if (!request.getBody().isPresent()) {
            res.setRetcode(ServerStatus.INVALID_REQUEST);
            res.setRetmessage(ServerMessage.INVALID_REQUEST);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build());
            return middlewareData;
        }
        middlewareData.setSuccess(true);
        return middlewareData;
    }
}
