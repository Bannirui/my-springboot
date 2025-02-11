package com.github.bannirui.msb.dubbo.envent;

import com.github.bannirui.msb.listener.spring.ComponentScanEventData;
import org.apache.dubbo.config.spring.ServiceBean;

public class DubboServiceScanedEvent implements ComponentScanEventData {
    private ServiceBean<Object> serviceBean;

    public DubboServiceScanedEvent(ServiceBean<Object> serviceBean) {
        this.serviceBean = serviceBean;
    }

    public ServiceBean<Object> getServiceBean() {
        return this.serviceBean;
    }

    public void setServiceBean(ServiceBean<Object> serviceBean) {
        this.serviceBean = serviceBean;
    }
}
