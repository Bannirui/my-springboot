package com.github.bannirui.msb.common.listener.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

public class ComponentScanEvent extends ApplicationContextEvent {

    private ComponentScanEventData componentScanEventData;

    private ComponentScanEvent(ApplicationContext source, ComponentScanEventData data) {
        super(source);
        this.componentScanEventData = data;
    }

    public ComponentScanEventData getComponentScanEventData() {
        return this.componentScanEventData;
    }

    public static ComponentScanEvent build(ApplicationContext applicationContext, ComponentScanEventData componentScanEventData) {
        return new ComponentScanEvent(applicationContext, componentScanEventData);
    }
}
