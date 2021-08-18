package com.dvc.models;

public class BaseDto {
    private String userid;
    private String username;
    private String atoken;
    private String browserinfo;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAtoken() {
        return atoken;
    }

    public void setAtoken(String atoken) {
        this.atoken = atoken;
    }

    /**
     * @return String return the browserinfo
     */
    public String getBrowserinfo() {
        return browserinfo;
    }

    /**
     * @param browserinfo the browserinfo to set
     */
    public void setBrowserinfo(String browserinfo) {
        this.browserinfo = browserinfo;
    }

}
