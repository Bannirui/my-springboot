package com.github.bannirui.msb.mq.sdk.metrics;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Distribution {
    private String name;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private LongAdder lessThan1Ms = new LongAdder();
    private LongAdder lessThan5Ms = new LongAdder();
    private LongAdder lessThan10Ms = new LongAdder();
    private LongAdder lessThan50Ms = new LongAdder();
    private LongAdder lessThan100Ms = new LongAdder();
    private LongAdder lessThan500Ms = new LongAdder();
    private LongAdder lessThan1000Ms = new LongAdder();
    private LongAdder moreThan1000Ms = new LongAdder();

    public Distribution() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Distribution newDistribution(String name) {
        Distribution distribution = new Distribution();
        distribution.setName(name);
        return distribution;
    }

    public void markTime(long costInMs) {
        if (costInMs < 1L) {
            this.lessThan1Ms.increment();
        } else if (costInMs < 5L) {
            this.lessThan5Ms.increment();
        } else if (costInMs < 10L) {
            this.lessThan10Ms.increment();
        } else if (costInMs < 50L) {
            this.lessThan50Ms.increment();
        } else if (costInMs < 100L) {
            this.lessThan100Ms.increment();
        } else if (costInMs < 500L) {
            this.lessThan500Ms.increment();
        } else if (costInMs < 1000L) {
            this.lessThan1000Ms.increment();
        } else {
            this.moreThan1000Ms.increment();
        }

    }

    public void markSize(long costInMs) {
        if (costInMs < 1024L) {
            this.lessThan1Ms.increment();
        } else if (costInMs < 5120L) {
            this.lessThan5Ms.increment();
        } else if (costInMs < 10240L) {
            this.lessThan10Ms.increment();
        } else if (costInMs < 51200L) {
            this.lessThan50Ms.increment();
        } else if (costInMs < 102400L) {
            this.lessThan100Ms.increment();
        } else if (costInMs < 512000L) {
            this.lessThan500Ms.increment();
        } else if (costInMs < 1048576L) {
            this.lessThan1000Ms.increment();
        } else {
            this.moreThan1000Ms.increment();
        }

    }

    public String output() {
        return System.currentTimeMillis() + "\n 1  - " + this.lessThan1Ms + "\n 5 - " + this.lessThan5Ms + "\n 10 - " + this.lessThan10Ms + "\n 50 - " + this.lessThan50Ms + "\n 100 - " + this.lessThan100Ms + "\n 500 - " + this.lessThan500Ms + "\n 1000 - " + this.lessThan1000Ms + "\n 1000more - " + this.moreThan1000Ms;
    }

    public LongAdder getLessThan1Ms() {
        return this.lessThan1Ms;
    }

    public void setLessThan1Ms(LongAdder lessThan1Ms) {
        this.lessThan1Ms = lessThan1Ms;
    }

    public LongAdder getLessThan5Ms() {
        return this.lessThan5Ms;
    }

    public void setLessThan5Ms(LongAdder lessThan5Ms) {
        this.lessThan5Ms = lessThan5Ms;
    }

    public LongAdder getLessThan10Ms() {
        return this.lessThan10Ms;
    }

    public void setLessThan10Ms(LongAdder lessThan10Ms) {
        this.lessThan10Ms = lessThan10Ms;
    }

    public LongAdder getLessThan50Ms() {
        return this.lessThan50Ms;
    }

    public void setLessThan50Ms(LongAdder lessThan50Ms) {
        this.lessThan50Ms = lessThan50Ms;
    }

    public LongAdder getLessThan100Ms() {
        return this.lessThan100Ms;
    }

    public void setLessThan100Ms(LongAdder lessThan100Ms) {
        this.lessThan100Ms = lessThan100Ms;
    }

    public LongAdder getLessThan500Ms() {
        return this.lessThan500Ms;
    }

    public void setLessThan500Ms(LongAdder lessThan500Ms) {
        this.lessThan500Ms = lessThan500Ms;
    }

    public LongAdder getLessThan1000Ms() {
        return this.lessThan1000Ms;
    }

    public void setLessThan1000Ms(LongAdder lessThan1000Ms) {
        this.lessThan1000Ms = lessThan1000Ms;
    }

    public LongAdder getMoreThan1000Ms() {
        return this.moreThan1000Ms;
    }

    public void setMoreThan1000Ms(LongAdder moreThan1000Ms) {
        this.moreThan1000Ms = moreThan1000Ms;
    }
}
