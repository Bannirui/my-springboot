package com.github.bannirui.msb.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 关注Apollo配置变更的对象 一旦配置发生变更需要执行回调
 * 反射执行对象的setter方法进行注入
 */
public class ApolloValue {
    /**
     * <ul>
     *     <li>key 配置的PropertyName</li>
     *     <li>value 配置发生变更要回调的地方 反射setter方法注入</li>
     * </ul>
     */
    private static final Map<String, ApolloValue> apollo_value_map = new HashMap<>();
    /**
     * Bean实例.
     */
    private Object obj;
    /**
     * Bean实例的setter方法.
     */
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

    public static Map<String, ApolloValue> getApolloValueMap() {
        return apollo_value_map;
    }
}
