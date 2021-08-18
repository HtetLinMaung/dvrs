package com.dvc.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidationResult {
    private boolean valid = true;
    private List<ResultInfo> infos;
    private List<Map<String, Object>> datalist;

    private List<Map<String, Object>> getCopyDatalist() throws IOException {
        List<Map<String, Object>> newdatalist = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> m : datalist) {
            final String json = mapper.writeValueAsString(m);
            final Map<String, Object> map = mapper.readValue(json, Map.class);
            newdatalist.add(map);
        }
        return newdatalist;

    }

    public List<Map<String, Object>> getValidDatalist() throws IOException {
        return getCopyDatalist().stream().filter(item -> (Boolean) item.get("isvalid")).map(item -> {
            item.remove("isvalid");
            return item;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getInValidDatalist() throws IOException {
        return getCopyDatalist().stream().filter(item -> !((Boolean) item.get("isvalid"))).map(item -> {
            item.remove("isvalid");
            return item;
        }).collect(Collectors.toList());
    }

    public boolean isValid() {
        return valid;
    }

    public List<Map<String, Object>> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<Map<String, Object>> datalist) {
        this.datalist = datalist;
    }

    public List<ResultInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<ResultInfo> infos) {
        this.infos = infos;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

}
