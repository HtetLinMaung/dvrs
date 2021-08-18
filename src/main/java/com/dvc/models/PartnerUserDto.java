package com.dvc.models;

public class PartnerUserDto extends BaseDto {
    private String syskey;
    private String remark;
    private String role;
    private long dvrsuserid;
    private String dvrsusername;
    private long partnersyskey;
    private String emailaddress;

    public String getSyskey() {
        return syskey;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getDvrsuserid() {
        return dvrsuserid;
    }

    public void setDvrsuserid(long dvrsuserid) {
        this.dvrsuserid = dvrsuserid;
    }

    public long getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(long partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

    /**
     * @return String return the emailaddress
     */
    public String getEmailaddress() {
        return emailaddress;
    }

    /**
     * @param emailaddress the emailaddress to set
     */
    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    /**
     * @return String return the dvrsusername
     */
    public String getDvrsusername() {
        return dvrsusername;
    }

    /**
     * @param dvrsusername the dvrsusername to set
     */
    public void setDvrsusername(String dvrsusername) {
        this.dvrsusername = dvrsusername;
    }

}
