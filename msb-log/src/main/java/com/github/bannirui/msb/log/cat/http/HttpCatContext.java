package com.github.bannirui.msb.log.cat.http;

import com.dianping.cat.Cat;
import java.util.HashMap;
import java.util.Map;

public class HttpCatContext implements Cat.Context {

    private Map<String, String> properties = new HashMap<>();

    @Override
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return this.properties.get(key);
    }
}