package com.dvc.models;

public class CheckQRDto {
    private String userid;
    private String ytoken;
    private String accesskey;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getYtoken() {
        return ytoken;
    }

    public void setYtoken(String ytoken) {
        this.ytoken = ytoken;
    }

    public String getAccesskey() {
        return accesskey;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

}
