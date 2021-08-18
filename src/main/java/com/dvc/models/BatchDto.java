package com.dvc.models;

import java.util.List;
import java.util.Map;

public class BatchDto extends BaseDto {
    private String fileext;
    private String batchsyskey;
    private String file;
    private List<FileData> files;
    private String filename;
    private int count;
    private String partnersyskey = "1";
    private String pisyskey = "1";
    private List<Map<String, Object>> datalist;
    private List<BatchStatus> statuslist;
    private String remark;
    private String centerid;
    private String partnerid;

    public String getFile() {
        return file;
    }

    public String getFileext() {
        return fileext;
    }

    public void setFileext(String fileext) {
        this.fileext = fileext;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getCenterid() {
        return centerid;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<BatchStatus> getStatuslist() {
        return statuslist;
    }

    public void setStatuslist(List<BatchStatus> statuslist) {
        this.statuslist = statuslist;
    }

    public String getBatchsyskey() {
        return batchsyskey;
    }

    public void setBatchsyskey(String batchsyskey) {
        this.batchsyskey = batchsyskey;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<Map<String, Object>> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<Map<String, Object>> datalist) {
        this.datalist = datalist;
    }

    public String getPisyskey() {
        return pisyskey;
    }

    public void setPisyskey(String pisyskey) {
        this.pisyskey = pisyskey;
    }

    public String getPartnersyskey() {
        return partnersyskey;
    }

    public void setPartnersyskey(String partnersyskey) {
        this.partnersyskey = partnersyskey;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
