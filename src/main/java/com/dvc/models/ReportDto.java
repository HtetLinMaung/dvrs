package com.dvc.models;

import java.util.List;

public class ReportDto {
    private List<String> columns;
    private String centerid;
    private String partnersyskey;
    private boolean all;

    public List<String> getColumns() {
        return columns;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getCenterid() {
        return centerid;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }
}
