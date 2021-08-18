package com.dvc.middlewares;

import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.dvc.models.MiddlewareData;
import com.dvc.models.BaseResponse;
import com.dvc.utils.ServerUtil;
import com.dvc.utils.TokenUtil;

public class SecurityMiddleware {
    public static MiddlewareData checkAuth(HttpRequestMessage<Optional<String>> request)
            throws JsonMappingException, JsonProcessingException {
        MiddlewareData middlewareData = new MiddlewareData();
        BaseResponse res = new BaseResponse();
        if (!request.getHeaders().containsKey("authorization")) {
            res.setRetcode(ServerStatus.UNAUTHORIZED);
            res.setRetmessage(ServerMessage.UNAUTHORIZED);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            return middlewareData;
        }
        String btoken = request.getHeaders().get("authorization").replace("Bearer ", "");
        if (btoken.isEmpty()) {
            res.setRetcode(ServerStatus.UNAUTHORIZED);
            res.setRetmessage(ServerMessage.UNAUTHORIZED);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            return middlewareData;
        }
        if (TokenUtil.isTokenExpired(btoken, "b")) {
            res.setRetcode(ServerStatus.UNAUTHORIZED);
            res.setRetmessage(ServerMessage.TOKEN_EXPIRED);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            return middlewareData;
        }
        if (!ServerUtil.isBTokenAuth(btoken)) {
            res.setRetcode(ServerStatus.UNAUTHORIZED);
            res.setRetmessage(ServerMessage.UNAUTHORIZED);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            return middlewareData;
        }

        middlewareData.setTokenData(TokenUtil.getBTokenData(btoken));
        middlewareData.setSuccess(true);
        return middlewareData;
    }

    public static MiddlewareData checkAuthAdmin(HttpRequestMessage<Optional<String>> request)
            throws JsonMappingException, JsonProcessingException {
        MiddlewareData middlewareData = new MiddlewareData();
        BaseResponse res = new BaseResponse();
        String btoken = request.getHeaders().get("authorization").replace("Bearer ", "");
        if (!ServerUtil.isBTokenAuthAdmin(btoken)) {
            System.out.println("Not Admin");
            res.setRetcode(ServerStatus.UNAUTHORIZED);
            res.setRetmessage(ServerMessage.UNAUTHORIZED);
            middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            return middlewareData;
        }
        System.out.println("Admin");
        middlewareData.setTokenData(TokenUtil.getBTokenData(btoken));
        middlewareData.setSuccess(true);
        return middlewareData;
    }
}
