package com.dvc.models;

import java.util.ArrayList;

public class RecipentsData {
    private long syskey;
    private String RecipientsName;
    private String Gender;
    private int Age;
    private String NRIC;
    private String Occupation;
    private String MobilePhone;
    private String Address1;
    private long RecipientID;
    private String PI;
    private String Batch;
    private String CertificateID;
    private String qrToken;
    private String batchRefCode;
    private String pdfName;
    private String passport;
    private String nationality;
    private String fatherName;
    private String dob;
    private int voidStatus;
    private String firstdosedate;
    private String firstdosetime;
    private String seconddosetime;
    private ArrayList<VaccinationRecordData> vArrayList = new ArrayList<VaccinationRecordData>();

    public long getSyskey() {
        return syskey;
    }

    public void setSyskey(long syskey) {
        this.syskey = syskey;
    }

    public String getRecipientsName() {
        return RecipientsName;
    }

    public void setRecipientsName(String recipientsName) {
        RecipientsName = recipientsName;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public String getNRIC() {
        return NRIC;
    }

    public void setNRIC(String nRIC) {
        NRIC = nRIC;
    }

    public String getOccupation() {
        return Occupation;
    }

    public void setOccupation(String occupation) {
        Occupation = occupation;
    }

    public String getMobilePhone() {
        return MobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        MobilePhone = mobilePhone;
    }

    public String getAddress1() {
        return Address1;
    }

    public void setAddress1(String address1) {
        Address1 = address1;
    }

    public long getRecipientID() {
        return RecipientID;
    }

    public void setRecipientID(long recipientID) {
        RecipientID = recipientID;
    }

    public ArrayList<VaccinationRecordData> getvArrayList() {
        return vArrayList;
    }

    public void setvArrayList(ArrayList<VaccinationRecordData> vArrayList) {
        this.vArrayList = vArrayList;
    }

    public String getPI() {
        return PI;
    }

    public void setPI(String pI) {
        PI = pI;
    }

    public String getBatch() {
        return Batch;
    }

    public void setBatch(String batch) {
        Batch = batch;
    }

    public String getCertificateID() {
        return CertificateID;
    }

    public void setCertificateID(String certificateID) {
        CertificateID = certificateID;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getBatchRefCode() {
        return batchRefCode;
    }

    public void setBatchRefCode(String batchRefCode) {
        this.batchRefCode = batchRefCode;
    }

    public RecipentsData() {
        clearProperties();
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
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

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getVoidStatus() {
        return voidStatus;
    }

    public void setVoidStatus(int voidStatus) {
        this.voidStatus = voidStatus;
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

    private void clearProperties() {
        this.syskey = 0;
        this.RecipientsName = "";
        this.Gender = "";
        this.Age = 0;
        this.NRIC = "";
        this.Occupation = "";
        this.MobilePhone = "";
        this.Address1 = "";
        this.PI = "";
        this.Batch = "";
        this.RecipientID = 0;
        this.CertificateID = "";
        this.qrToken = "";
        this.batchRefCode = "";
        this.pdfName = "";
        this.passport = "";
        this.dob = "";
        this.nationality = "";
        this.fatherName = "";
        this.voidStatus = -1;
        this.firstdosedate = "";
        this.firstdosetime = "";
        this.seconddosetime = "";
        this.vArrayList = new ArrayList<VaccinationRecordData>();
    }
}