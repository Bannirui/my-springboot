package com.github.bannirui.msb.dubbo.envent;

import com.github.bannirui.msb.listener.spring.ComponentScanEventData;
import java.lang.reflect.Method;
import org.apache.dubbo.config.annotation.Reference;

public class DubboMethodRefScanedEvent implements ComponentScanEventData {
    private Method method;
    private Reference reference;
    private Object data;

    public DubboMethodRefScanedEvent(Method method, Reference reference, Object data) {
        this.method = method;
        this.reference = reference;
        this.data = data;
    }

    public Reference getReference() {
        return this.reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
