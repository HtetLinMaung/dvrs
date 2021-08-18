package com.dvc.middlewares;

import java.util.Optional;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.models.BaseResponse;
import com.dvc.models.MiddlewareData;
import com.dvc.utils.ServerUtil;
import com.dvc.utils.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;

public class SecurityFileDownloadMiddleware {
    public static MiddlewareData checkAuthorization(HttpRequestMessage<Optional<String>> request) {
        MiddlewareData middlewareData = new MiddlewareData();
        try {
            BaseResponse res = new BaseResponse();
            final String query = request.getQueryParameters().get("token");
            String btoken = request.getBody().orElse(query);
            if (btoken == null || btoken.isEmpty()) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            } else {
                if (btoken.contains("Bearer")) {
                    btoken = btoken.replace("Bearer ", "");
                }
                if (TokenUtil.isTokenExpired(btoken, "b")) {
                    res.setRetcode(ServerStatus.UNAUTHORIZED);
                    res.setRetmessage(ServerMessage.TOKEN_EXPIRED);
                    middlewareData
                            .setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
                }
                if (!ServerUtil.isBTokenAuth(btoken)) {
                    res.setRetcode(ServerStatus.UNAUTHORIZED);
                    res.setRetmessage(ServerMessage.UNAUTHORIZED);
                    middlewareData
                            .setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
                } else {
                    middlewareData.setTokenData(TokenUtil.getBTokenData(btoken));
                    middlewareData.setSuccess(true);
                }
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return middlewareData;
    }

    public static MiddlewareData checkIFAdminOrPartnerAuthorization(HttpRequestMessage<Optional<String>> request) {
        MiddlewareData middlewareData = new MiddlewareData();
        try {
            BaseResponse res = new BaseResponse();
            final String query = request.getQueryParameters().get("token");
            String btoken = request.getBody().orElse(query);
            if (btoken.contains("Bearer")) {
                btoken = btoken.replace("Bearer ", "");
            }
            if (TokenUtil.isTokenExpired(btoken, "b")) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.TOKEN_EXPIRED);
                middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            }
            if (!(ServerUtil.isBTokenAuthAdmin(btoken) || ServerUtil.isBTokenAuthPartner(btoken))) {
                res.setRetcode(ServerStatus.UNAUTHORIZED);
                res.setRetmessage(ServerMessage.UNAUTHORIZED);
                middlewareData.setResponse(request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body(res).build());
            } else {
                middlewareData.setTokenData(TokenUtil.getBTokenData(btoken));
                middlewareData.setSuccess(true);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return middlewareData;
    }
}