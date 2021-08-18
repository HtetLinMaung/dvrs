package com.dvc.models;

import java.util.List;

public class PaginationResponse<T> extends BaseResponse {
    private int currentpage;
    private int pagecount;
    private int pagesize;
    private int totalcount;
    private List<T> datalist;
    private String validcount;
    private String invalidcount;

    public int getCurrentpage() {
        return currentpage;
    }

    public String getInvalidcount() {
        return invalidcount;
    }

    public void setInvalidcount(String invalidcount) {
        this.invalidcount = invalidcount;
    }

    public String getValidcount() {
        return validcount;
    }

    public void setValidcount(String validcount) {
        this.validcount = validcount;
    }

    public void setCurrentpage(int currentpage) {
        this.currentpage = currentpage;
    }

    public int getPagecount() {
        return pagecount;
    }

    public void setPagecount(int pagecount) {
        this.pagecount = pagecount;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public int getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(int totalcount) {
        this.totalcount = totalcount;
    }

    public List<T> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<T> datalist) {
        this.datalist = datalist;
    }

}
