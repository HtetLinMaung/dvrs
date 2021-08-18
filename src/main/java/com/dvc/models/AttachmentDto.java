package com.dvc.models;

import java.util.List;

public class AttachmentDto extends BaseDto {
    private String syskey;
    private String pisyskey;
    private List<FileData> files;

    public String getSyskey() {
        return syskey;
    }

    public void setSyskey(String syskey) {
        this.syskey = syskey;
    }

    public String getPisyskey() {
        return pisyskey;
    }

    public void setPisyskey(String pisyskey) {
        this.pisyskey = pisyskey;
    }

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

}
