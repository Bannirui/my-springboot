package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;

public interface MessageTree extends Cloneable {

    MessageTree copy();

    String getDomain();

    String getHostName();

    String getIpAddress();

    Message getMessage();

    String getMessageId();

    String getParentMessageId();

    String getRootMessageId();

    String getSessionToken();

    String getThreadGroupName();

    String getThreadId();

    String getThreadName();

    boolean isSample();

    void setDomain(String domain);

    void setHostName(String hostName);

    void setIpAddress(String ipAddress);

    void setMessage(Message message);

    void setMessageId(String messageId);

    void setParentMessageId(String parentMessageId);

    void setRootMessageId(String rootMessageId);

    void setSessionToken(String sessionToken);

    void setThreadGroupName(String threadGroupName);

    void setThreadId(String threadId);

    void setThreadName(String threadName);

    void setSample(boolean sample);
}
