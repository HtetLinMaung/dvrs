package com.dvc.models;

import com.microsoft.azure.functions.ExecutionContext;

public class FilterDto extends BaseDto {
    private int pagesize;
    private int currentpage;
    private String search;
    private String partnersyskey = "";
    private String centerid = "";
    private String batchuploadsyskey = "";
    private int detailstatus;
    private String role;
    private boolean all;

    private int dosecount;
    private int operator; // 0 => = , 1 => >
    private boolean alldose;

    private String voidstatus = "";
    private String recordstatus = "";
    private String doseupdatedate = "";
    private boolean reverse = true;

    private String startcid = "";
    private String endcid = "";

    private String groupcode = "";
    private String subgroupcode = "";

    private ExecutionContext context;

    public String getGroupcode() {
        return groupcode;
    }

    public ExecutionContext getContext() {
        return context;
    }

    public void setContext(ExecutionContext context) {
        this.context = context;
    }

    public void setGroupcode(String groupcode) {
        this.groupcode = groupcode;
    }

    public String getSubgroupcode() {
        return subgroupcode;
    }

    public void setSubgroupcode(String subgroupcode) {
        this.subgroupcode = subgroupcode;
    }

    public String getStartcid() {
        return startcid;
    }

    public void setStartcid(String startcid) {
        this.startcid = startcid;
    }

    public String getEndcid() {
        return endcid;
    }

    public void setEndcid(String endcid) {
        this.endcid = endcid;
    }

    public int getDosecount() {
        return dosecount;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public String getDoseupdatedate() {
        return doseupdatedate;
    }

    public void setDoseupdatedate(String doseupdatedate) {
        this.doseupdatedate = doseupdatedate;
    }

    public String getRecordstatus() {
        return recordstatus;
    }

    public void setRecordstatus(String recordstatus) {
        this.recordstatus = recordstatus;
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
