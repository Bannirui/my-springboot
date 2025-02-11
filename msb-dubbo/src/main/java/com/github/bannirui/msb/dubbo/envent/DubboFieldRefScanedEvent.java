package com.github.bannirui.msb.dubbo.envent;

import com.github.bannirui.msb.listener.spring.ComponentScanEventData;
import java.lang.reflect.Field;
import org.apache.dubbo.config.annotation.Reference;

public class DubboFieldRefScanedEvent implements ComponentScanEventData {
    private Field field;
    private Reference reference;
    private Object data;

    public DubboFieldRefScanedEvent(Field field, Reference reference, Object data) {
        this.field = field;
        this.reference = reference;
        this.data = data;
    }

    public Field getField() {
        return this.field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Reference getReference() {
        return this.reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }
}
