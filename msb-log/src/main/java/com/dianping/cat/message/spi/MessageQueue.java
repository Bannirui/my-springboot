package com.dianping.cat.message.spi;

public interface MessageQueue {
    boolean offer(MessageTree tree);

    boolean offer(MessageTree tree, double sampleRatio);

    MessageTree peek();

    MessageTree poll();

    int size();
}
