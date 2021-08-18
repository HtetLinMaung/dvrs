package com.dvc.models;

public class ComboData {
    private Object value;
    private String description;

    public ComboData(String description, Object value) {
        this.value = value;
        this.description = description;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
