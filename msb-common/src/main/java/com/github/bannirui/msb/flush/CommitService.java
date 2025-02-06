package com.github.bannirui.msb.flush;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitService<T> implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(CommitService.class);
    private final MsbCountDownLatch waitPoint;
    private volatile List<CommitRequest<T>> requestsWrite;
    private volatile List<CommitRequest<T>> requestsRead;
    protected volatile boolean stopped;
    private final long interval;
    private IFlush flush;
    protected volatile AtomicBoolean hasNotified;

    public CommitService(IFlush flush) {
        this(flush, 10L);
    }

    public CommitService(IFlush flush, final long interval) {
        this.waitPoint = new MsbCountDownLatch(1);
        this.requestsWrite = new ArrayList();
        this.requestsRead = new ArrayList();
        this.stopped = false;
        this.hasNotified = new AtomicBoolean(false);
        this.flush = flush;
        this.interval = interval;
    }

    public void putRequest(final CommitRequest<T> request) {
        synchronized (this.requestsWrite) {
            this.requestsWrite.add(request);
        }

        if (this.hasNotified.compareAndSet(false, true)) {
            this.waitPoint.countDown();
        }

    }

    private void swapRequests() {
        List<CommitRequest<T>> tmp = this.requestsWrite;
        this.requestsWrite = this.requestsRead;
        this.requestsRead = tmp;
    }

    private void doFlush() {
        synchronized (this.requestsRead) {
            if (!this.requestsRead.isEmpty()) {
                boolean isFlush = this.flush.flush(this.requestsRead);
                this.requestsRead.stream().forEach((req) -> {
                    req.wakeup(isFlush);
                });
                this.requestsRead.clear();
            }

        }
    }

    public void run() {
        Exception e;
        while (!this.isStopped()) {
            try {
                this.waitForRunning(this.interval);
                this.doFlush();
            } catch (Exception var3) {
                e = var3;
                logger.warn("flush service has exception. ", e);
            }
        }

        try {
            Thread.sleep(this.interval);
        } catch (Exception var5) {
            e = var5;
            logger.warn("flush Service Exception, ", e);
        }

        synchronized (this) {
            this.swapRequests();
        }

        this.doFlush();
    }

    protected void waitForRunning(long interval) {
        if (this.hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
        } else {
            this.waitPoint.reset();

            try {
                this.waitPoint.await(interval, TimeUnit.MILLISECONDS);
            } catch (Exception var7) {
                Exception e = var7;
                logger.error(e.getMessage(), e);
            } finally {
                this.hasNotified.set(false);
                this.onWaitEnd();
            }

        }
    }

    protected void onWaitEnd() {
        this.swapRequests();
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public interface IFlush<T> {
        boolean flush(List<CommitRequest<T>> requests);
    }

}
