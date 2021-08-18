package com.dvc.models;

public class CenterDto {
    private String syskey;
    private String centerid;
    private String centername;
    private int allowblank;
    private String price;

    public String getCenterid() {
        return centerid;
    }

    public String getSyskey() {
        return syskey;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

    public String getCentername() {
        return centername;
    }

    public void setCentername(String centername) {
        this.centername = centername;
    }

    public int getAllowblank() {
        return allowblank;
    }

    public void setAllowblank(int allowblank) {
        this.allowblank = allowblank;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
