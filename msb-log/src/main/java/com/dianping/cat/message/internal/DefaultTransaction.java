package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultTransaction extends AbstractMessage implements Transaction {

    private long m_durationInMicro = -1L;
    private List<Message> m_children;
    private MessageManager m_manager;
    private boolean m_standalone;
    private long m_durationStart;

    public DefaultTransaction(String type, String name, MessageManager manager) {
        super(type, name);
        this.m_manager = manager;
        this.m_standalone = true;
        this.m_durationStart = System.nanoTime();
    }

    public DefaultTransaction addChild(Message message) {
        if (this.m_children == null) {
            this.m_children = new ArrayList<>();
        }
        if (message != null) {
            this.m_children.add(message);
        } else {
            Cat.logError(new Exception("null child message"));
        }
        return this;
    }

    @Override
    public void complete() {
        try {
            if (this.isCompleted()) {
                DefaultEvent event = new DefaultEvent("cat", "BadInstrument");
                event.setStatus("TransactionAlreadyCompleted");
                event.complete();
                this.addChild(event);
            } else {
                this.m_durationInMicro = (System.nanoTime() - this.m_durationStart) / 1000L;
                this.setCompleted(true);
                if (this.m_manager != null) {
                    this.m_manager.end(this);
                }
            }
        } catch (Exception var2) {
        }
    }

    public List<Message> getChildren() {
        return this.m_children == null ? Collections.emptyList() : this.m_children;
    }

    @Override
    public long getDurationInMicros() {
        if (this.m_durationInMicro >= 0L) {
            return this.m_durationInMicro;
        } else {
            long duration = 0L;
            int len = this.m_children == null ? 0 : this.m_children.size();
            if (len > 0) {
                Message lastChild = this.m_children.get(len - 1);
                if (lastChild instanceof Transaction) {
                    DefaultTransaction trx = (DefaultTransaction) lastChild;
                    duration = (trx.getTimestamp() - this.getTimestamp()) * 1000L;
                } else {
                    duration = (lastChild.getTimestamp() - this.getTimestamp()) * 1000L;
                }
            }
            return duration;
        }
    }

    @Override
    public long getDurationInMillis() {
        return this.getDurationInMicros() / 1000L;
    }

    protected MessageManager getManager() {
        return this.m_manager;
    }

    @Override
    public boolean hasChildren() {
        return this.m_children != null && this.m_children.size() > 0;
    }

    @Override
    public boolean isStandalone() {
        return this.m_standalone;
    }

    public void setDurationInMicros(long duration) {
        this.m_durationInMicro = duration;
    }

    public void setDurationInMillis(long duration) {
        this.m_durationInMicro = duration * 1000L;
    }

    public void setStandalone(boolean standalone) {
        this.m_standalone = standalone;
    }

    public void setDurationStart(long durationStart) {
        this.m_durationStart = durationStart;
    }
}
