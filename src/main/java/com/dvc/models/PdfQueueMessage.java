package com.dvc.models;

public class PdfQueueMessage {
    private int offset;
    private long batchNo;
    private boolean isOverride;
    private long partnerSyskey;
    private boolean isAdmin;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public long getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public void setOverride(boolean isOverride) {
        this.isOverride = isOverride;
    }

    public long getPartnerSyskey() {
        return partnerSyskey;
    }

    public void setPartnerSyskey(long partnerSyskey) {
        this.partnerSyskey = partnerSyskey;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

}