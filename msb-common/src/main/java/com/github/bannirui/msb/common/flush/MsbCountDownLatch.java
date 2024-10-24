package com.github.bannirui.msb.common.flush;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class MsbCountDownLatch {

    private final MsbSync sync;

    public MsbCountDownLatch(int cnt) {
        if (cnt < 0) {
            throw new IllegalArgumentException("cnt<0");
        }
        this.sync = new MsbSync(cnt);
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public long getCount() {
        return this.sync.getCount();
    }

    public void reset() {
        this.sync.reset();
    }

    @Override
    public String toString() {
        return "MsbCountDownLatch{" +
            "sync=" + sync +
            '}';
    }

    private static final class MsbSync extends AbstractQueuedSynchronizer {
        private final int state;

        MsbSync(int state) {
            this.state = state;
            super.setState(state);
        }

        int getCount() {
            return this.state;
        }

        @Override
        protected int tryAcquireShared(int arg) {
            return super.getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            int c, nextc;
            do {
                c = super.getState();
                if (c == 0) {
                    return false;
                }
                nextc = c - 1;
            } while (!super.compareAndSetState(c, nextc));
            return nextc == 0;
        }

        protected void reset() {
            super.setState(this.state);
        }
    }
}
