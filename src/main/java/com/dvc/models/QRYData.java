package com.dvc.models;

public class QRYData {
    private String syskey;
    private String cid;
    private String name;
    private String nric;
    private String dob;

    private String url;

    private String qraction;

    public String getSyskey() {
        return syskey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getQraction() {
        return qraction;
    }

    public void setQraction(String qraction) {
        this.qraction = qraction;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

}
