package com.github.bannirui.msb.remotecfg.spring.bean;

public class FieldValueAnnotationAttr extends ValueAnnotationAttr {

    public FieldValueAnnotationAttr(String placeHolder, String propertyName, Class<?> propertyType) {
        super(placeHolder, propertyName, propertyType);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.FIELD;
    }
}
