package com.github.bannirui.msb.flush;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractFlushService<T> implements CommitService.IFlush<T> {

    private final ExecutorService executorService;
    private final CommitService<T> commitService;

    public AbstractFlushService() {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(10), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                long tid = t.getId();
                t.setName("FlushService-pool-" + tid);
                return t;
            }
        }, new ThreadPoolExecutor.AbortPolicy());
        this.commitService = new CommitService<>(this, 10);
    }

    public void start() {
        this.executorService.execute(this.commitService);
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    public void putRequest(CommitRequest<T> request) {
        this.commitService.putRequest(request);
    }
}
