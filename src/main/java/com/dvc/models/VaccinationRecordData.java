package com.dvc.models;

public class VaccinationRecordData {
    private long syskey;
    private String VaccineLotNo;
    private long ApplicationsSysKey;
    private String remark;
    private String VaccineCompany;
    private String PersonName;
    private int Vaccination;
    private String CenterName;
    private long CenterSysKey;
    private String VaccinationDate;
    private String NextVaccinationDate;
    private int VaccinationStatus;
    private String VaccineSerialNo;
    private String t1;
    private String t2;
    private String t3;
    private String t4;
    private String t5;
    private long n1;
    private long n2;
    private long n3;
    private long n4;
    private long n5;

    public long getSyskey() {
        return syskey;
    }

    public void setSyskey(long syskey) {
        this.syskey = syskey;
    }

    public String getVaccineLotNo() {
        return VaccineLotNo;
    }

    public void setVaccineLotNo(String vaccineLotNo) {
        VaccineLotNo = vaccineLotNo;
    }

    public long getApplicationsSysKey() {
        return ApplicationsSysKey;
    }

    public void setApplicationsSysKey(long applicationsSysKey) {
        ApplicationsSysKey = applicationsSysKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getVaccineCompany() {
        return VaccineCompany;
    }

    public void setVaccineCompany(String vaccineCompany) {
        VaccineCompany = vaccineCompany;
    }

    public String getPersonName() {
        return PersonName;
    }

    public void setPersonName(String personName) {
        PersonName = personName;
    }

    public int getVaccination() {
        return Vaccination;
    }

    public void setVaccination(int vaccination) {
        Vaccination = vaccination;
    }

    public String getCenterName() {
        return CenterName;
    }

    public void setCenterName(String centerName) {
        CenterName = centerName;
    }

    public long getCenterSysKey() {
        return CenterSysKey;
    }

    public void setCenterSysKey(long centerSysKey) {
        CenterSysKey = centerSysKey;
    }

    public String getVaccinationDate() {
        return VaccinationDate;
    }

    public void setVaccinationDate(String vaccinationDate) {
        VaccinationDate = vaccinationDate;
    }

    public String getNextVaccinationDate() {
        return NextVaccinationDate;
    }

    public void setNextVaccinationDate(String nextVaccinationDate) {
        NextVaccinationDate = nextVaccinationDate;
    }

    public int getVaccinationStatus() {
        return VaccinationStatus;
    }

    public void setVaccinationStatus(int vaccinationStatus) {
        VaccinationStatus = vaccinationStatus;
    }

    public String getVaccineSerialNo() {
        return VaccineSerialNo;
    }

    public void setVaccineSerialNo(String vaccineSerialNo) {
        VaccineSerialNo = vaccineSerialNo;
    }

    public String getT1() {
        return t1;
    }

    public void setT1(String t1) {
        this.t1 = t1;
    }

    public String getT2() {
        return t2;
    }

    public void setT2(String t2) {
        this.t2 = t2;
    }

    public String getT3() {
        return t3;
    }

    public void setT3(String t3) {
        this.t3 = t3;
    }

    public String getT4() {
        return t4;
    }

    public void setT4(String t4) {
        this.t4 = t4;
    }

    public String getT5() {
        return t5;
    }

    public void setT5(String t5) {
        this.t5 = t5;
    }

    public long getN1() {
        return n1;
    }

    public void setN1(long n1) {
        this.n1 = n1;
    }

    public long getN2() {
        return n2;
    }

    public void setN2(long n2) {
        this.n2 = n2;
    }

    public long getN3() {
        return n3;
    }

    public void setN3(long n3) {
        this.n3 = n3;
    }

    public long getN4() {
        return n4;
    }

    public void setN4(long n4) {
        this.n4 = n4;
    }

    public long getN5() {
        return n5;
    }

    public void setN5(long n5) {
        this.n5 = n5;
    }

    private void clearProperties() {
        this.syskey = 0;
        this.VaccineLotNo = "";
        this.ApplicationsSysKey = 0;
        this.remark = "";
        this.VaccineCompany = "";
        this.PersonName = "";
        this.Vaccination = 0;
        this.CenterName = "";
        this.CenterSysKey = 0;
        this.VaccinationDate = "";
        this.NextVaccinationDate = "";
        this.VaccinationStatus = 0;
        this.VaccineSerialNo = "";
        this.t1 = "";
        this.t2 = "";
        this.t3 = "";
        this.t4 = "";
        this.t5 = "";
        this.n1 = 0;
        this.n2 = 0;
        this.n3 = 0;
        this.n4 = 0;
        this.n5 = 0;
    }
}