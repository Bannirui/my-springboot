package com.dianping.cat.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KVConfig {
    private Map<String, String> m_kvs = new HashMap<>();

    public Set<String> getKeys() {
        return this.m_kvs.keySet();
    }

    public Map<String, String> getKvs() {
        return this.m_kvs;
    }

    public String getValue(String key) {
        return this.m_kvs.get(key);
    }

    public void setKvs(Map<String, String> kvs) {
        this.m_kvs = kvs;
    }
}
