package com.dvc.models;

public class StatusDto extends BaseDto {
    private int voidstatus;
    private String syskey;

    public int getVoidstatus() {
        return voidstatus;
    }

    public String getSyskey() {
        return syskey;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public void setVoidstatus(int voidstatus) {
        this.voidstatus = voidstatus;
    }
}
