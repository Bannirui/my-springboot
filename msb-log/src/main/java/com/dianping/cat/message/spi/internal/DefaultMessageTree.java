package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.nio.charset.StandardCharsets;

public class DefaultMessageTree implements MessageTree {
    private ByteBuf m_buf;
    private String m_domain;
    private String m_hostName;
    private String m_ipAddress;
    private Message m_message;
    private String m_messageId;
    private String m_parentMessageId;
    private String m_rootMessageId;
    private String m_sessionToken;
    private String m_threadGroupName;
    private String m_threadId;
    private String m_threadName;
    private boolean m_sample = true;

    public MessageTree copy() {
        MessageTree tree = new DefaultMessageTree();
        tree.setDomain(this.m_domain);
        tree.setHostName(this.m_hostName);
        tree.setIpAddress(this.m_ipAddress);
        tree.setMessageId(this.m_messageId);
        tree.setParentMessageId(this.m_parentMessageId);
        tree.setRootMessageId(this.m_rootMessageId);
        tree.setSessionToken(this.m_sessionToken);
        tree.setThreadGroupName(this.m_threadGroupName);
        tree.setThreadId(this.m_threadId);
        tree.setThreadName(this.m_threadName);
        tree.setMessage(this.m_message);
        tree.setSample(this.m_sample);
        return tree;
    }

    public ByteBuf getBuffer() {
        return this.m_buf;
    }

    @Override
    public String getDomain() {
        return this.m_domain;
    }

    @Override
    public String getHostName() {
        return this.m_hostName;
    }

    @Override
    public String getIpAddress() {
        return this.m_ipAddress;
    }

    @Override
    public Message getMessage() {
        return this.m_message;
    }

    @Override
    public String getMessageId() {
        return this.m_messageId;
    }

    @Override
    public String getParentMessageId() {
        return this.m_parentMessageId;
    }

    @Override
    public String getRootMessageId() {
        return this.m_rootMessageId;
    }

    @Override
    public String getSessionToken() {
        return this.m_sessionToken;
    }

    @Override
    public String getThreadGroupName() {
        return this.m_threadGroupName;
    }

    @Override
    public String getThreadId() {
        return this.m_threadId;
    }

    @Override
    public String getThreadName() {
        return this.m_threadName;
    }

    @Override
    public boolean isSample() {
        return this.m_sample;
    }

    public void setBuffer(ByteBuf buf) {
        this.m_buf = buf;
    }

    @Override
    public void setDomain(String domain) {
        this.m_domain = domain;
    }

    @Override
    public void setHostName(String hostName) {
        this.m_hostName = hostName;
    }

    @Override
    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    @Override
    public void setMessage(Message message) {
        this.m_message = message;
    }

    @Override
    public void setMessageId(String messageId) {
        if (messageId != null && messageId.length() > 0) {
            this.m_messageId = messageId;
        }
    }

    @Override
    public void setParentMessageId(String parentMessageId) {
        if (parentMessageId != null && parentMessageId.length() > 0) {
            this.m_parentMessageId = parentMessageId;
        }
    }

    @Override
    public void setRootMessageId(String rootMessageId) {
        if (rootMessageId != null && rootMessageId.length() > 0) {
            this.m_rootMessageId = rootMessageId;
        }
    }

    @Override
    public void setSample(boolean sample) {
        this.m_sample = sample;
    }

    @Override
    public void setSessionToken(String sessionToken) {
        this.m_sessionToken = sessionToken;
    }

    @Override
    public void setThreadGroupName(String threadGroupName) {
        this.m_threadGroupName = threadGroupName;
    }

    @Override
    public void setThreadId(String threadId) {
        this.m_threadId = threadId;
    }

    @Override
    public void setThreadName(String threadName) {
        this.m_threadName = threadName;
    }

    @Override
    public String toString() {
        PlainTextMessageCodec codec = new PlainTextMessageCodec();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        codec.encode(this, buf);
        buf.readInt();
        codec.reset();
        return buf.toString(StandardCharsets.UTF_8);
    }
}
