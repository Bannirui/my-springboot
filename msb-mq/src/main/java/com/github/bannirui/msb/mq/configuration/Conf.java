package com.github.bannirui.msb.mq.configuration;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Conf {
    private String consumerGroup;
    private String tag;
    private List<Map<String, Object>> params;
    private Object obj;
    private Method method;

    public Object getObj() {
        return this.obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Map<String, Object>> getParams() {
        return this.params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }
}
