package com.github.bannirui.msb.orm.squence;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceRange {
    private final long min;
    private final long max;
    private final AtomicLong value;
    private volatile boolean over = false;

    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
    }

    public long getBatch(int size) {
        if (this.over) {
            return -1L;
        } else {
            long currentValue = this.value.getAndAdd((long)size) + (long)size - 1L;
            if (currentValue > this.max) {
                this.over = true;
                return -1L;
            } else {
                return currentValue;
            }
        }
    }

    public long getAndIncrement() {
        if (this.over) {
            return -1L;
        } else {
            long currentValue = this.value.getAndIncrement();
            if (currentValue > this.max) {
                this.over = true;
                return -1L;
            } else {
                return currentValue;
            }
        }
    }

    public long getMin() {
        return this.min;
    }

    public long getMax() {
        return this.max;
    }

    public boolean isOver() {
        return this.over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("max: ").append(this.max).append(", min: ").append(this.min).append(", value: ").append(this.value);
        return sb.toString();
    }
}
