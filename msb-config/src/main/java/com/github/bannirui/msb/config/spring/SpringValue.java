package com.github.bannirui.msb.config.spring;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.springframework.core.MethodParameter;

public class SpringValue {
    private MethodParameter methodParameter;
    private Field field;
    private WeakReference<Object> beanRef;
    private String beanName;
    private String key;
    private String placeholder;
    private Class<?> targetType;
    private Type genericType;
    private boolean isJson;

    public SpringValue(String key, String placeholder, Object bean, String beanName, Field field, boolean isJson) {
        this.beanRef = new WeakReference(bean);
        this.beanName = beanName;
        this.field = field;
        this.key = key;
        this.placeholder = placeholder;
        this.targetType = field.getType();
        this.isJson = isJson;
        if (isJson) {
            this.genericType = field.getGenericType();
        }

    }

    public SpringValue(String key, String placeholder, Object bean, String beanName, Method method, boolean isJson) {
        this.beanRef = new WeakReference(bean);
        this.beanName = beanName;
        this.methodParameter = new MethodParameter(method, 0);
        this.key = key;
        this.placeholder = placeholder;
        Class<?>[] paramTps = method.getParameterTypes();
        this.targetType = paramTps[0];
        this.isJson = isJson;
        if (isJson) {
            this.genericType = method.getGenericParameterTypes()[0];
        }

    }

    public void update(Object newVal) throws IllegalAccessException, InvocationTargetException {
        if (this.isField()) {
            this.injectField(newVal);
        } else {
            this.injectMethod(newVal);
        }

    }

    private void injectField(Object newVal) throws IllegalAccessException {
        Object bean = this.beanRef.get();
        if (bean != null) {
            boolean accessible = this.field.isAccessible();
            this.field.setAccessible(true);
            this.field.set(bean, newVal);
            this.field.setAccessible(accessible);
        }
    }

    private void injectMethod(Object newVal) throws InvocationTargetException, IllegalAccessException {
        Object bean = this.beanRef.get();
        if (bean != null) {
            this.methodParameter.getMethod().invoke(bean, newVal);
        }
    }

    public String getBeanName() {
        return this.beanName;
    }

    public Class<?> getTargetType() {
        return this.targetType;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public MethodParameter getMethodParameter() {
        return this.methodParameter;
    }

    public boolean isField() {
        return this.field != null;
    }

    public Field getField() {
        return this.field;
    }

    public Type getGenericType() {
        return this.genericType;
    }

    public boolean isJson() {
        return this.isJson;
    }

    boolean isTargetBeanValid() {
        return this.beanRef.get() != null;
    }

    public String toString() {
        Object bean = this.beanRef.get();
        if (bean == null) {
            return "";
        } else {
            return this.isField() ?
                String.format("key: %s, beanName: %s, field: %s.%s", this.key, this.beanName, bean.getClass().getName(), this.field.getName()) :
                String.format("key: %s, beanName: %s, method: %s.%s", this.key, this.beanName, bean.getClass().getName(),
                    this.methodParameter.getMethod().getName());
        }
    }

}
