package com.dvc.models;

import java.util.List;

public class ReportDto {
    private List<String> columns;
    private String centerid;
    private String partnersyskey;
    private boolean all;
    private int dosecount;
    private int operator;
    private String role;

    public int getDosecount() {
        return dosecount;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
