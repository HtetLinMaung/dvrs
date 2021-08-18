package com.dvc.models;

public class PIDto {
    private String syskey;
    private String remark;
    private String pirefnumber;
    private String attention;
    private int qty;
    private String price;
    private String taxamount;
    private String total;
    private String paymentdate;
    private String branch;
    private String partnersyskey;
    private String amount;
    private String paymentref;
    private String contact;
    private String bankname;
    private String centerid;
    private int applicantcount;
    private int paymentstatus = 0;
    private int recordstatus = 0;
    private String approveddate;
    private String accountnumber;

    private String batchsyskey;
    private int statustype;

    public int getApplicantcount() {
        return applicantcount;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public int getStatustype() {
        return statustype;
    }

    public void setStatustype(int statustype) {
        this.statustype = statustype;
    }

    public String getApproveddate() {
        return approveddate;
    }

    public void setApproveddate(String approveddate) {
        this.approveddate = approveddate;
    }

    public int getRecordstatus() {
        return recordstatus;
    }

    public void setRecordstatus(int recordstatus) {
        this.recordstatus = recordstatus;
    }

    public void setApplicantcount(int applicantcount) {
        this.applicantcount = applicantcount;
    }

    public int getPaymentstatus() {
        return paymentstatus;
    }

    public void setPaymentstatus(int paymentstatus) {
        this.paymentstatus = paymentstatus;
    }

    public String getPaymentref() {
        return paymentref;
    }

    public String getCenterid() {
        return centerid;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public void setPaymentref(String paymentref) {
        this.paymentref = paymentref;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSyskey() {
        return syskey;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBatchsyskey() {
        return batchsyskey;
    }

    public void setBatchsyskey(String batchsyskey) {
        this.batchsyskey = batchsyskey;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
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

    public String getPirefnumber() {
        return pirefnumber;
    }

    public void setPirefnumber(String pirefnumber) {
        this.pirefnumber = pirefnumber;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPaymentdate() {
        return paymentdate;
    }

    public void setPaymentdate(String paymentdate) {
        this.paymentdate = paymentdate;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

}
