package com.dvc.models;

import com.microsoft.azure.functions.HttpResponseMessage;

public class MiddlewareData {
    private HttpResponseMessage response;
    private TokenData tokenData;
    private boolean success = false;

    public HttpResponseMessage getResponse() {
        return response;
    }

    public void setResponse(HttpResponseMessage response) {
        this.response = response;
    }

    public TokenData getTokenData() {
        return tokenData;
    }

    public void setTokenData(TokenData tokenData) {
        this.tokenData = tokenData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
