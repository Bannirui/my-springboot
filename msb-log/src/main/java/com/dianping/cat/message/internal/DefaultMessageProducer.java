package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.github.bannirui.msb.common.util.StringUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.unidal.lookup.annotation.Inject;

public class DefaultMessageProducer implements MessageProducer {
    @Inject
    private MessageManager m_manager;
    @Inject
    private MessageIdFactory m_factory;

    @Override
    public String createMessageId() {
        return this.m_factory.getNextId();
    }

    @Override
    public boolean isEnabled() {
        return this.m_manager.isMessageEnabled();
    }

    @Override
    public void logError(String message, Throwable cause) {
        /**
         * cat-client的client.xml配置domain的enabled
         */
        if(!Cat.getManager().isCatEnabled()) {
            cause.printStackTrace();
            return;
        }
        if (!this.shouldLog(cause)) return;
        this.m_manager.getThreadLocalMessageTree().setSample(false);
        StringWriter writer = new StringWriter(2048);
        if (message != null) {
            writer.write(message);
            // 空格
            writer.write(32);
        }
        cause.printStackTrace(new PrintWriter(writer));
        String detailMessage = writer.toString();
        if (cause instanceof Error) {
            this.logEvent("Error", cause.getClass().getName(), "ERROR", detailMessage);
        } else if (cause instanceof RuntimeException) {
            this.logEvent("RuntimeException", cause.getClass().getName(), "ERROR", detailMessage);
        } else {
            this.logEvent("Exception", cause.getClass().getName(), "ERROR", detailMessage);
        }
    }

    @Override
    public void logError(Throwable cause) {
        this.logError(null, cause);
    }

    @Override
    public void logEvent(String type, String name) {
        this.logEvent(type, name, "0", (String) null);
    }

    @Override
    public void logEvent(String type, String name, String status, String nameValuePairs) {
        Event event = this.newEvent(type, name);
        if(StringUtil.isNotBlank(nameValuePairs)) {
            event.addData(nameValuePairs);
        }
        event.setStatus(status);
        event.complete();
    }

    @Override
    public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
        Heartbeat heartbeat = this.newHeartbeat(type, name);
        heartbeat.addData(nameValuePairs);
        heartbeat.setStatus(status);
        heartbeat.complete();
    }

    @Override
    public void logMetric(String name, String status, String nameValuePairs) {
        String type = "";
        Metric metric = this.newMetric(type, name);
        if(StringUtil.isNotBlank(nameValuePairs)) {
            metric.addData(nameValuePairs);
        }
        metric.setStatus(status);
        metric.complete();
    }

    @Override
    public void logTrace(String type, String name) {
        this.logTrace(type, name, "0", null);
    }

    @Override
    public void logTrace(String type, String name, String status, String nameValuePairs) {
        if (this.m_manager.isTraceMode()) {
            Trace trace = this.newTrace(type, name);
            if(StringUtil.isNotBlank(nameValuePairs)) {
                trace.addData(nameValuePairs);
            }
            trace.setStatus(status);
            trace.complete();
        }
    }

    @Override
    public Event newEvent(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            return new DefaultEvent(type, name, this.m_manager);
        } else {
            return NullMessage.EVENT;
        }
    }

    public Event newEvent(Transaction parent, String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled() && parent != null) {
            DefaultEvent event = new DefaultEvent(type, name);
            parent.addChild(event);
            return event;
        } else {
            return NullMessage.EVENT;
        }
    }

    @Override
    public ForkedTransaction newForkedTransaction(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            MessageTree tree = this.m_manager.getThreadLocalMessageTree();
            if (tree.getMessageId() == null) {
                tree.setMessageId(this.createMessageId());
            }
            DefaultForkedTransaction transaction = new DefaultForkedTransaction(type, name, this.m_manager);
            if (this.m_manager instanceof DefaultMessageManager dmm) {
                dmm.linkAsRunAway(transaction);
            }
            this.m_manager.start(transaction, true);
            return transaction;
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    @Override
    public Heartbeat newHeartbeat(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name, this.m_manager);
            this.m_manager.getThreadLocalMessageTree().setSample(false);
            return heartbeat;
        } else {
            return NullMessage.HEARTBEAT;
        }
    }

    @Override
    public Metric newMetric(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            DefaultMetric metric = new DefaultMetric(type == null ? "" : type, name, this.m_manager);
            this.m_manager.getThreadLocalMessageTree().setSample(false);
            return metric;
        } else {
            return NullMessage.METRIC;
        }
    }

    @Override
    public TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            MessageTree tree = this.m_manager.getThreadLocalMessageTree();
            if (tree.getMessageId() == null) {
                tree.setMessageId(this.createMessageId());
            }
            DefaultTaggedTransaction transaction = new DefaultTaggedTransaction(type, name, tag, this.m_manager);
            this.m_manager.start(transaction, true);
            return transaction;
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    @Override
    public Trace newTrace(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            return new DefaultTrace(type, name, this.m_manager);
        } else {
            return NullMessage.TRACE;
        }
    }

    @Override
    public Transaction newTransaction(String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }
        if (this.m_manager.isMessageEnabled()) {
            DefaultTransaction transaction = new DefaultTransaction(type, name, this.m_manager);
            this.m_manager.start(transaction, false);
            return transaction;
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    public Transaction newTransaction(Transaction parent, String type, String name) {
        if (!this.m_manager.hasContext()) {
            this.m_manager.setup();
        }

        if (this.m_manager.isMessageEnabled() && parent != null) {
            DefaultTransaction transaction = new DefaultTransaction(type, name, this.m_manager);
            parent.addChild(transaction);
            transaction.setStandalone(false);
            return transaction;
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    private boolean shouldLog(Throwable e) {
        return !(this.m_manager instanceof DefaultMessageManager dmm) || dmm.shouldLog(e);
    }
}
