package com.github.bannirui.msb.orm.squence;

import com.github.bannirui.msb.ex.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractSequence implements Sequence {
    private static final Logger logger = LoggerFactory.getLogger(GroupSequence.class);
    protected volatile SequenceRange currentRange;
    protected final Lock lock = new ReentrantLock();
    protected SequenceDao sequenceDao;

    public void init() throws SQLException {
        this.sequenceDao = this.getSequenceDao();
        Exception ex = null;
        String sequenceName = this.getSequenceName();
        synchronized(this) {
            int i = 0;

            while(i < this.sequenceDao.getRetryTimes()) {
                try {
                    this.sequenceDao.adjust(sequenceName);
                    ex = null;
                    break;
                } catch (Exception var7) {
                    ex = var7;
                    logger.warn("Sequence第" + (i + 1) + "次初始化失败, name:" + sequenceName, var7);
                    ++i;
                }
            }
        }

        if (ex != null) {
            logger.error("Sequence初始化失败，切重试" + this.sequenceDao.getRetryTimes() + "次后，仍然失败! name:" + sequenceName, ex);
            throw new RuntimeException(ex);
        }
    }

    abstract String getSequenceName();

    abstract SequenceDao getSequenceDao();

    public long nextValue() {
        if (this.isInitRange()) {
            this.lock.lock();

            try {
                if (this.isInitRange()) {
                    this.setCurrentRange(this.sequenceDao.nextRange(this.getSequenceName()));
                }
            } finally {
                this.lock.unlock();
            }
        }

        long value = this.getSequenceRange().getAndIncrement();
        if (value == -1L) {
            this.lock.lock();

            try {
                do {
                    if (this.getSequenceRange().isOver()) {
                        this.setCurrentRange(this.sequenceDao.nextRange(this.getSequenceName()));
                    }

                    value = this.getSequenceRange().getAndIncrement();
                } while(value == -1L);
            } finally {
                this.lock.unlock();
            }
        }

        if (value < 0L) {
            throw FrameworkException.getInstance("Sequence value overflow, name = {} value = {}", new Object[]{this.getSequenceName(), value});
        } else {
            return value;
        }
    }

    protected boolean isInitRange() {
        return this.getSequenceRange() == null;
    }

    private SequenceRange getSequenceRange() {
        return this.currentRange;
    }

    public long nextValue(int type) {
        throw new RuntimeException("未实现nextValue(int size) 方法");
    }

    public boolean exhaustValue() {
        throw new RuntimeException("未实现exhaustValue()方法");
    }

    public SequenceRange getCurrentRange() {
        return this.currentRange;
    }

    public void setCurrentRange(SequenceRange currentRange) {
        this.currentRange = currentRange;
    }
}
