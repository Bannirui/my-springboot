package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public interface MessageManager {

    void add(Message message);

    void end(Transaction transaction);

    Transaction getPeekTransaction();

    MessageTree getThreadLocalMessageTree();

    boolean hasContext();

    boolean isMessageEnabled();

    boolean isCatEnabled();

    boolean isTraceMode();

    void reset();

    void setTraceMode(boolean traceMode);

    void setup();

    void start(Transaction transaction, boolean forked);

    void bind(String tag, String title);

    String getDomain();
}
