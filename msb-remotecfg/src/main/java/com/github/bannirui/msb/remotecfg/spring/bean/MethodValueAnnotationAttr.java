package com.github.bannirui.msb.remotecfg.spring.bean;

import java.lang.reflect.Method;

/**
 * {@link org.springframework.beans.factory.annotation.Value}注解打在了方法上.
 * 将来反射的时候需要类的方法.
 */
public class MethodValueAnnotationAttr extends ValueAnnotationAttr{

    /**
     * 注解打在了哪个方法上.
     */
    private Method method;

    public MethodValueAnnotationAttr(String placeHolder, String propertyName, Class<?> propertyType, Method method) {
        super(placeHolder, propertyName, propertyType);
        this.method = method;
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.METHOD;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
