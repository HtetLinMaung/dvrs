package com.dvc.models;

public class RecipientsDto extends BaseDto {
    private String syskey;
    private String remark;
    private String recipientsname;
    private String fathername;
    private String gender;
    private String dob;
    private String nric;
    private String passport;
    private String nationality;
    private String organization;
    private String address1;
    private String township;
    private String division;
    private String mobilephone;
    private int vaccinationstatus;
    private String partnersyskey;
    private int serialno;

    public String getSyskey() {
        return syskey;
    }

    public int getSerialno() {
        return serialno;
    }

    public void setSerialno(int serialno) {
        this.serialno = serialno;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

    public int getVaccinationstatus() {
        return vaccinationstatus;
    }

    public void setVaccinationstatus(int vaccinationstatus) {
        this.vaccinationstatus = vaccinationstatus;
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

    public String getRecipientsname() {
        return recipientsname;
    }

    public void setRecipientsname(String recipientsname) {
        this.recipientsname = recipientsname;
    }

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getTownship() {
        return township;
    }

    public void setTownship(String township) {
        this.township = township;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

}
