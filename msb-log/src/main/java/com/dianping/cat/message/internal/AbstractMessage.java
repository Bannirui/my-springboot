package com.dianping.cat.message.internal;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.nio.charset.StandardCharsets;

public abstract class AbstractMessage implements Message {

    private String m_type;
    private String m_name;
    private String m_status = "unset";
    private long m_timestampInMillis;
    private CharSequence m_data;
    private boolean m_completed;

    public AbstractMessage(String type, String name) {
        this.m_type = String.valueOf(type);
        this.m_name = String.valueOf(name);
        this.m_timestampInMillis = MilliSecondTimer.currentTimeMillis();
    }

    @Override
    public void addData(String keyValuePairs) {
        if (this.m_data == null) {
            this.m_data = keyValuePairs;
        } else if (this.m_data instanceof StringBuilder) {
            ((StringBuilder) this.m_data).append('&').append(keyValuePairs);
        } else {
            StringBuilder sb = new StringBuilder(this.m_data.length() + keyValuePairs.length() + 16);
            sb.append(this.m_data).append('&');
            sb.append(keyValuePairs);
            this.m_data = sb;
        }
    }

    @Override
    public void addData(String key, Object value) {
        if (this.m_data instanceof StringBuilder) {
            ((StringBuilder) this.m_data).append('&').append(key).append('=').append(value);
        } else {
            String str = String.valueOf(value);
            int old = this.m_data == null ? 0 : this.m_data.length();
            StringBuilder sb = new StringBuilder(old + key.length() + str.length() + 16);
            if (this.m_data != null) {
                sb.append(this.m_data).append('&');
            }
            sb.append(key).append('=').append(str);
            this.m_data = sb;
        }
    }

    @Override
    public CharSequence getData() {
        return (CharSequence) (this.m_data == null ? "" : this.m_data);
    }

    @Override
    public String getName() {
        return this.m_name;
    }

    @Override
    public String getStatus() {
        return this.m_status;
    }

    @Override
    public long getTimestamp() {
        return this.m_timestampInMillis;
    }

    @Override
    public String getType() {
        return this.m_type;
    }

    @Override
    public boolean isCompleted() {
        return this.m_completed;
    }

    @Override
    public boolean isSuccess() {
        return "0".equals(this.m_status);
    }

    public void setCompleted(boolean completed) {
        this.m_completed = completed;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    @Override
    public void setStatus(String status) {
        this.m_status = status;
    }

    @Override
    public void setStatus(Throwable e) {
        this.m_status = e.getClass().getName();
    }

    public void setTimestamp(long timestamp) {
        this.m_timestampInMillis = timestamp;
    }

    public void setType(String type) {
        this.m_type = type;
    }

    @Override
    public String toString() {
        PlainTextMessageCodec codec = new PlainTextMessageCodec();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        codec.encodeMessage(this, buf);
        codec.reset();
        return buf.toString(StandardCharsets.UTF_8);
    }
}
