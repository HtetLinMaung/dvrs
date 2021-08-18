package com.dvc.models;

public class UserHistoryDto {
    long syskey;
    int type;
    String ipaddress;
    PartnerDto partnerDto;
    PartnerUserDto partnerUserDto;

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public long getSyskey() {
        return syskey;
    }

    public PartnerDto getPartnerDto() {
        return partnerDto;
    }

    public void setPartnerDto(PartnerDto partnerDto) {
        this.partnerDto = partnerDto;
    }

    public PartnerUserDto getPartnerUserDto() {
        return partnerUserDto;
    }

    public void setPartnerUserDto(PartnerUserDto partnerUserDto) {
        this.partnerUserDto = partnerUserDto;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSyskey(long userHistorySyskey) {
        this.syskey = userHistorySyskey;
    }

}
