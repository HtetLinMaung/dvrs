package com.dvc.models;

import java.util.Map;

public class CheckQRResponse extends BaseResponse {
    private String url;
    private Map<String, Object> data;

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
