package com.dianping.cat.message.internal;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultMetric extends AbstractMessage implements Metric {

    private MessageManager m_manager;

    public DefaultMetric(String type, String name) {
        super(type, name);
    }

    public DefaultMetric(String type, String name, MessageManager manager) {
        super(type, name);
        this.m_manager = manager;
    }

    @Override
    public void complete() {
        this.setCompleted(true);
        if (this.m_manager != null) {
            this.m_manager.add(this);
        }
    }
}
