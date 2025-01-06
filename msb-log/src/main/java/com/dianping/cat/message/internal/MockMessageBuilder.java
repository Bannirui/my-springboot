package com.dianping.cat.message.internal;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class MockMessageBuilder {

    private Stack<TransactionHolder> m_stack = new Stack<>();

    public final Message build() {
        Message ans = null;
        try {
            ans = this.define().build();
        } finally {
            this.m_stack.clear();
        }
        return ans;
    }

    public abstract MockMessageBuilder.MessageHolder define();

    protected MockMessageBuilder.EventHolder e(String type, String name) {
        MockMessageBuilder.EventHolder e = new MockMessageBuilder.EventHolder(type, name);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        return e;
    }

    protected MockMessageBuilder.EventHolder e(String type, String name, String data) {
        MockMessageBuilder.EventHolder e = new MockMessageBuilder.EventHolder(type, name, data);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        return e;
    }

    protected MockMessageBuilder.HeartbeatHolder h(String type, String name) {
        MockMessageBuilder.HeartbeatHolder h = new MockMessageBuilder.HeartbeatHolder(type, name);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            h.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        return h;
    }

    protected MockMessageBuilder.MetricHolder m(String type, String name) {
        MockMessageBuilder.MetricHolder e = new MockMessageBuilder.MetricHolder(type, name);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        return e;
    }

    protected MockMessageBuilder.MetricHolder m(String type, String name, String data) {
        MockMessageBuilder.MetricHolder e = new MockMessageBuilder.MetricHolder(type, name, data);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        return e;
    }

    protected MockMessageBuilder.TransactionHolder t(String type, String name, long durationInMillis) {
        MockMessageBuilder.TransactionHolder t = new MockMessageBuilder.TransactionHolder(type, name, durationInMillis);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            t.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        this.m_stack.push(t);
        return t;
    }

    protected MockMessageBuilder.TransactionHolder t(String type, String name, String data, long durationInMillis) {
        MockMessageBuilder.TransactionHolder t = new MockMessageBuilder.TransactionHolder(type, name, data, durationInMillis);
        MockMessageBuilder.TransactionHolder parent = this.m_stack.isEmpty() ? null : (MockMessageBuilder.TransactionHolder) this.m_stack.peek();
        if (parent != null) {
            t.setTimestampInMicros(parent.getCurrentTimestampInMicros());
        }
        this.m_stack.push(t);
        return t;
    }

    protected class TransactionHolder extends MockMessageBuilder.AbstractMessageHolder {
        private long m_durationInMicros;
        private long m_currentTimestampInMicros;
        private List<MessageHolder> m_children = new ArrayList<>();
        private DefaultTransaction m_transaction;
        private long m_markTimestampInMicros;

        public TransactionHolder(String type, String name, long durationInMicros) {
            super(type, name);
            this.m_durationInMicros = durationInMicros;
        }

        public TransactionHolder(String type, String name, String data, long durationInMicros) {
            super(type, name, data);
            this.m_durationInMicros = durationInMicros;
        }

        public MockMessageBuilder.TransactionHolder after(long periodInMicros) {
            this.m_currentTimestampInMicros += periodInMicros;
            return this;
        }

        public MockMessageBuilder.TransactionHolder at(long timestampInMillis) {
            this.m_currentTimestampInMicros = timestampInMillis * 1000L;
            this.setTimestampInMicros(this.m_currentTimestampInMicros);
            return this;
        }

        public Transaction build() {
            this.m_transaction = new DefaultTransaction(this.getType(), this.getName(), null);
            this.m_transaction.setTimestamp(this.getTimestampInMillis());
            this.m_children.forEach(x -> this.m_transaction.addChild(x.build()));
            this.m_transaction.setStatus(this.getStatus());
            this.m_transaction.addData(this.getData());
            this.m_transaction.complete();
            this.m_transaction.setDurationInMicros(this.m_durationInMicros);
            return this.m_transaction;
        }

        public MockMessageBuilder.TransactionHolder child(MockMessageBuilder.MessageHolder child) {
            if (child instanceof MockMessageBuilder.TransactionHolder ch) {
                this.m_currentTimestampInMicros += ch.getDurationInMicros();
                MockMessageBuilder.this.m_stack.pop();
            }
            this.m_children.add(child);
            return this;
        }

        public MockMessageBuilder.TransactionHolder data(String key, String value) {
            this.addData(key, value);
            return this;
        }

        public long getCurrentTimestampInMicros() {
            return this.m_currentTimestampInMicros;
        }

        public long getDurationInMicros() {
            return this.m_durationInMicros;
        }

        public MockMessageBuilder.TransactionHolder mark() {
            this.m_markTimestampInMicros = this.m_currentTimestampInMicros;
            return this;
        }

        public MockMessageBuilder.TransactionHolder reset() {
            this.m_currentTimestampInMicros = this.m_markTimestampInMicros;
            return this;
        }

        public void setTimestampInMicros(long timestampInMicros) {
            super.setTimestampInMicros(timestampInMicros);
            this.m_currentTimestampInMicros = timestampInMicros;
        }

        public MockMessageBuilder.TransactionHolder status(String status) {
            this.setStatus(status);
            return this;
        }
    }

    protected static class MetricHolder extends MockMessageBuilder.AbstractMessageHolder {
        private DefaultMetric m_metric;

        public MetricHolder(String type, String name) {
            super(type, name);
        }

        public MetricHolder(String type, String name, String data) {
            super(type, name, data);
        }

        public Metric build() {
            this.m_metric = new DefaultMetric(this.getType(), this.getName());
            this.m_metric.setTimestamp(this.getTimestampInMillis());
            this.m_metric.setStatus(this.getStatus());
            this.m_metric.addData(this.getData());
            this.m_metric.complete();
            return this.m_metric;
        }

        public MockMessageBuilder.MetricHolder status(String status) {
            this.setStatus(status);
            return this;
        }
    }

    protected interface MessageHolder {
        Message build();

        long getTimestampInMicros();

        void setTimestampInMicros(long var1);
    }

    protected static class HeartbeatHolder extends MockMessageBuilder.AbstractMessageHolder {
        private DefaultHeartbeat m_heartbeat;

        public HeartbeatHolder(String type, String name) {
            super(type, name);
        }

        public Heartbeat build() {
            this.m_heartbeat = new DefaultHeartbeat(this.getType(), this.getName());
            this.m_heartbeat.setTimestamp(this.getTimestampInMillis());
            this.m_heartbeat.setStatus(this.getStatus());
            this.m_heartbeat.complete();
            return this.m_heartbeat;
        }

        public MockMessageBuilder.HeartbeatHolder status(String status) {
            this.setStatus(status);
            return this;
        }
    }

    public static class EventHolder extends MockMessageBuilder.AbstractMessageHolder {
        private DefaultEvent m_event;

        public EventHolder(String type, String name) {
            super(type, name);
        }

        public EventHolder(String type, String name, String data) {
            super(type, name, data);
        }

        public Event build() {
            this.m_event = new DefaultEvent(this.getType(), this.getName(), null);
            this.m_event.setTimestamp(this.getTimestampInMillis());
            this.m_event.setStatus(this.getStatus());
            this.m_event.addData(this.getData());
            this.m_event.complete();
            return this.m_event;
        }

        public MockMessageBuilder.EventHolder status(String status) {
            this.setStatus(status);
            return this;
        }
    }

    protected abstract static class AbstractMessageHolder implements MockMessageBuilder.MessageHolder {
        private String m_type;
        private String m_name;
        private String m_data;
        private long m_timestampInMicros;
        private String m_status = "0";

        public AbstractMessageHolder(String type, String name) {
            this.m_type = type;
            this.m_name = name;
        }

        public AbstractMessageHolder(String type, String name, String data) {
            this.m_type = type;
            this.m_name = name;
            this.m_data = data;
        }

        public void addData(String key, String value) {
            if (this.m_data == null) {
                this.m_data = key + "=" + value;
            } else {
                this.m_data = this.m_data + "&" + key + "=" + value;
            }
        }

        public String getData() {
            return this.m_data;
        }

        public String getName() {
            return this.m_name;
        }

        public String getStatus() {
            return this.m_status;
        }

        public long getTimestampInMicros() {
            return this.m_timestampInMicros;
        }

        public long getTimestampInMillis() {
            return this.m_timestampInMicros / 1_000L;
        }

        public String getType() {
            return this.m_type;
        }

        public void setStatus(String status) {
            this.m_status = status;
        }

        @Override
        public void setTimestampInMicros(long timestampInMicros) {
            this.m_timestampInMicros = timestampInMicros;
        }
    }
}
