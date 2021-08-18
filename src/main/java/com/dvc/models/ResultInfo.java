package com.dvc.models;

import java.util.List;

public class ResultInfo {
    private int linenumber;
    private List<String> descriptionlist;
    private List<String> keys;

    public int getLinenumber() {
        return linenumber;
    }

    public List<String> getDescriptionlist() {
        return descriptionlist;
    }

    public void setDescriptionlist(List<String> descriptionlist) {
        this.descriptionlist = descriptionlist;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void setLinenumber(int linenumber) {
        this.linenumber = linenumber;
    }

}
