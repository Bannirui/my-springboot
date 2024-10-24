package com.github.bannirui.msb.common.flush;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitRequest<T> {

    private static Logger logger = LoggerFactory.getLogger(CommitRequest.class);

    private final T data;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile boolean flushOk = false;

    public CommitRequest(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public void wakeup(final boolean flushOk) {
        this.flushOk = flushOk;
        this.countDownLatch.countDown();
    }

    /**
     * @param timeout 毫秒
     */
    public boolean waitForFlush(long timeout) {
        try {
            return this.countDownLatch.await(timeout, TimeUnit.MILLISECONDS) ? this.flushOk : false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
