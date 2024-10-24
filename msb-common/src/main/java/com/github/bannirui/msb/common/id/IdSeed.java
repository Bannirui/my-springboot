package com.github.bannirui.msb.common.id;

import java.util.concurrent.atomic.AtomicLong;

public class IdSeed {

    private final long timeStamp;
    private AtomicLong sequence;

    public IdSeed(long timeStamp) {
        this.timeStamp = timeStamp;
        this.sequence = new AtomicLong(0L);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long increment() {
        return this.sequence.getAndIncrement();
    }
}
