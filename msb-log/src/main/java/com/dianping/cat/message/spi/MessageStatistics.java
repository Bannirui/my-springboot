package com.dianping.cat.message.spi;

public interface MessageStatistics {
    long getBytes();

    long getOverflowed();

    long getProduced();

    void onBytes(int bytes);

    void onOverflowed(MessageTree tree);
}
