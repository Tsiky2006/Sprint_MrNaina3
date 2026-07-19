package com.framework.core;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private Map<String, Object> data = new HashMap<>();

    public void setAttribute(String key, Object value) {
        data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}