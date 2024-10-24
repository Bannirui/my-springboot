package com.github.bannirui.msb.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApolloValue {
    private static Map<String, ApolloValue> apolloValueMap = new HashMap();
    private Object obj;
    private Method method;

    public ApolloValue() {
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

    public static Map<String, ApolloValue> getApolloValueMap() {
        return apolloValueMap;
    }
}
