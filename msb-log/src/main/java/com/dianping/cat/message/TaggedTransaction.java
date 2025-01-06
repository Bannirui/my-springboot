package com.dianping.cat.message;

public interface TaggedTransaction extends Transaction {

    void bind(String tag, String childMessageId, String title);

    String getParentMessageId();

    String getRootMessageId();

    String getTag();

    void start();
}
