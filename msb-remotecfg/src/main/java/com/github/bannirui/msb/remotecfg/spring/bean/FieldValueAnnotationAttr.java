package com.github.bannirui.msb.remotecfg.spring.bean;

import java.lang.reflect.Field;

public class FieldValueAnnotationAttr extends ValueAnnotationAttr {

    /**
     * 注解打在了哪个成员上.
     */
    private Field field;

    public FieldValueAnnotationAttr(String placeHolder, String propertyName, Class<?> propertyType, Field field) {
        super(placeHolder, propertyName, propertyType);
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.FIELD;
    }
}
