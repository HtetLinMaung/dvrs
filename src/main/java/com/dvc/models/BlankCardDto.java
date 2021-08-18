package com.dvc.models;

public class BlankCardDto extends BaseDto {
    private String pisyskey;
    private String partnersyskey = "";
    private String batchsyskey;

    public String getPisyskey() {
        return pisyskey;
    }

    public String getBatchsyskey() {
        return batchsyskey;
    }

    public void setBatchsyskey(String batchsyskey) {
        this.batchsyskey = batchsyskey;
    }

    public void setPisyskey(String pisyskey) {
        this.pisyskey = pisyskey;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

}
