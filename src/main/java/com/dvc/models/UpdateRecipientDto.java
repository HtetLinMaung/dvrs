package com.dvc.models;

import com.dvc.utils.ValidateBatchUtils;

public class UpdateRecipientDto {
    private String cid;
    private String nric;
    private String passport;
    private String gender;
    private String dob;
    private String recipientsname;
    private String fathername;
    private String nationality;
    private String mobilephone;
    private String address1;
    private String firstdosedate;
    private String firstdosetime;
    private String seconddosetime;
    private String userid;
    private String username;

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return ValidateBatchUtils.getAge(dob);
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getRecipientsname() {
        return recipientsname;
    }

    public void setRecipientsname(String recipientsname) {
        this.recipientsname = recipientsname;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getFirstdosedate() {
        return firstdosedate;
    }

    public void setFirstdosedate(String firstdosedate) {
        this.firstdosedate = firstdosedate;
    }

    public String getFirstdosetime() {
        return firstdosetime;
    }

    public void setFirstdosetime(String firstdosetime) {
        this.firstdosetime = firstdosetime;
    }

    public String getSeconddosetime() {
        return seconddosetime;
    }

    public void setSeconddosetime(String seconddosetime) {
        this.seconddosetime = seconddosetime;
    }

}
