package com.github.bannirui.msb.listener;

import java.lang.reflect.Method;

public class ConfigChangeListenerMetaData {
    private Object obj;
    private Method method;
    private String[] paramNames;

    public ConfigChangeListenerMetaData(Object target, Method method, String[] paramNames) {
        this.obj = target;
        this.method = method;
        this.paramNames = paramNames;
    }

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

    public String[] getParamNames() {
        return this.paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }
}
