package com.dianping.cat.message.internal;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import java.util.Collections;
import java.util.List;

public enum NullMessage implements Transaction, Event, Metric, Trace, Heartbeat, ForkedTransaction, TaggedTransaction {
    TRANSACTION,
    EVENT,
    METRIC,
    TRACE,
    HEARTBEAT;

    private NullMessage() {
    }

    @Override
    public Transaction addChild(Message message) {
        return this;
    }

    @Override
    public void addData(String keyValuePairs) {
    }

    @Override
    public void addData(String key, Object value) {
    }

    public void bind(String tag, String childMessageId, String title) {
    }

    @Override
    public void complete() {
    }

    public void fork() {
    }

    @Override
    public List<Message> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public long getDurationInMicros() {
        return 0L;
    }

    @Override
    public long getDurationInMillis() {
        return 0L;
    }

    public String getForkedMessageId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getParentMessageId() {
        return null;
    }

    public String getRootMessageId() {
        return null;
    }

    @Override
    public String getStatus() {
        throw new UnsupportedOperationException();
    }

    public String getTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isStandalone() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public void setStatus(String status) {
    }

    @Override
    public void setStatus(Throwable e) {
    }

    public void start() {
    }
}
