package com.dianping.cat.message;

public interface ForkedTransaction extends Transaction {

    void fork();

    String getForkedMessageId();
}
