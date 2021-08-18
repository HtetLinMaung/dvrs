package com.dvc.models;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;

public class BaseResponse {
    private String retcode = ServerStatus.SUCCESS;
    private String retmessage = ServerMessage.SUCCESS;

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetmessage() {
        return retmessage;
    }

    public void setRetmessage(String retmessage) {
        this.retmessage = retmessage;
    }

}
