package com.dvc.utils;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EasyData<T> {
    private T obj;

    public EasyData(T obj) {
        this.obj = obj;
    }

    public Map<String, Object> toMap() throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(mapper.writeValueAsString(obj), Map.class);
    }

    public Map<String, Object> toMapExcept(List<String> exceptlist)
            throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(mapper.writeValueAsString(obj), Map.class);
        for (String key : exceptlist) {
            map.remove(key);
        }
        return map;
    }
}
