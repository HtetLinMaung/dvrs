package com.dvc.models;

public class BatchStatus {
    private String syskey;
    private int status;
    private int statustype; // 1 approval, 2 payment, 3 void

    public String getSyskey() {
        return syskey;
    }

    public int getStatustype() {
        return statustype;
    }

    public void setStatustype(int statustype) {
        this.statustype = statustype;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

}
