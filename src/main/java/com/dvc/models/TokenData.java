package com.dvc.models;

public class TokenData {
    private String userid;
    private String appid;
    private String partnersyskey;
    private String dvrsuserid;
    private String role;
    private String partnerid;
    private String partnertype;
    private String partnername;
    private String contactperson;
    private String userlevel;
    private String dvrsusername;

    public String getUserid() {
        return userid;
    }

    public String getDvrsusername() {
        return dvrsusername;
    }

    public void setDvrsusername(String dvrsusername) {
        this.dvrsusername = dvrsusername;
    }

    public String getUserlevel() {
        return userlevel;
    }

    public void setUserlevel(String userlevel) {
        this.userlevel = userlevel;
    }

    public String getContactperson() {
        return contactperson;
    }

    public void setContactperson(String contactperson) {
        this.contactperson = contactperson;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

    public String getDvrsuserid() {
        return dvrsuserid;
    }

    public void setDvrsuserid(String dvrsuserid) {
        this.dvrsuserid = dvrsuserid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPartnertype() {
        return partnertype;
    }

    public void setPartnertype(String partnertype) {
        this.partnertype = partnertype;
    }

    public String getPartnername() {
        return partnername;
    }

    public void setPartnername(String partnername) {
        this.partnername = partnername;
    }

}
