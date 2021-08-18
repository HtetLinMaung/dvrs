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
