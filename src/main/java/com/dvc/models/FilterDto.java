package com.dvc.models;

public class FilterDto extends BaseDto {
    private int pagesize;
    private int currentpage;
    private String search;
    private String partnersyskey = "";
    private String centerid = "";
    private String batchuploadsyskey;
    private int detailstatus;
    private String role;
    private boolean all;

    private int dosecount;
    private int operator; // 0 => = , 1 => >
    private boolean alldose;

    private String voidstatus = "";

    public int getDosecount() {
        return dosecount;
    }

    public String getVoidstatus() {
        return voidstatus;
    }

    public void setVoidstatus(String voidstatus) {
        this.voidstatus = voidstatus;
    }

    public void setDosecount(int dosecount) {
        this.dosecount = dosecount;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public boolean isAlldose() {
        return alldose;
    }

    public void setAlldose(boolean alldose) {
        this.alldose = alldose;
    }

    public int getPagesize() {
        return pagesize;
    }

    public String getCenterid() {
        return centerid;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getDetailstatus() {
        return detailstatus;
    }

    public void setDetailstatus(int detailstatus) {
        this.detailstatus = detailstatus;
    }

    public String getBatchuploadsyskey() {
        return batchuploadsyskey;
    }

    public void setBatchuploadsyskey(String batchuploadsyskey) {
        this.batchuploadsyskey = batchuploadsyskey;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public int getCurrentpage() {
        return currentpage;
    }

    public void setCurrentpage(int currentpage) {
        this.currentpage = currentpage;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
