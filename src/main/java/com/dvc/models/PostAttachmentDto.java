package com.dvc.models;

import java.util.List;

public class PostAttachmentDto {
    private String syskey;
    private String batchsyskey;
    private List<FileData> files;

    public String getSyskey() {
        return syskey;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public String getBatchsyskey() {
        return batchsyskey;
    }

    public void setBatchsyskey(String batchsyskey) {
        this.batchsyskey = batchsyskey;
    }

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

}
