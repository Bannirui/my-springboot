package com.github.bannirui.msb.http.event;

import com.github.bannirui.msb.listener.spring.ComponentScanEventData;

public class HttpServiceComponentScanedEventData implements ComponentScanEventData {
    private Class<?> interfaceClazz;

    public HttpServiceComponentScanedEventData(Class<?> interfaceClazz) {
        this.setInterfaceClazz(interfaceClazz);
    }

    public Class<?> getInterfaceClazz() {
        return this.interfaceClazz;
    }

    public void setInterfaceClazz(Class<?> interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
    }
}
