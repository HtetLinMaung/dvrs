package com.dvc.models;

public class PostUploadDto {
    private String fileext;
    private String batchsyskey;
    private String file;
    private String filename;

    public String getFileext() {
        return fileext;
    }

    public void setFileext(String fileext) {
        this.fileext = fileext;
    }

    public String getBatchsyskey() {
        return batchsyskey;
    }

    public void setBatchsyskey(String batchsyskey) {
        this.batchsyskey = batchsyskey;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
