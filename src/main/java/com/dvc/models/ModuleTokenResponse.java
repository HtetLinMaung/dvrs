package com.dvc.models;

public class ModuleTokenResponse extends BaseResponse {
    private String btoken;
    private String role;
    private String partnername;
    private String partnerid;

    public String getRole() {
        return role;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPartnername() {
        return partnername;
    }

    public void setPartnername(String partnername) {
        this.partnername = partnername;
    }

    public String getBtoken() {
        return btoken;
    }

    public void setBtoken(String btoken) {
        this.btoken = btoken;
    }

}
