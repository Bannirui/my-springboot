package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class ThreadsInfo extends BaseEntity<ThreadsInfo> {
    private int m_count;
    private int m_daemonCount;
    private int m_peekCount;
    private int m_totalStartedCount;
    private int m_catThreadCount;
    private int m_pigeonThreadCount;
    private int m_httpThreadCount;
    private String m_dump;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitThread(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadsInfo) {
            ThreadsInfo _o = (ThreadsInfo) obj;
            if (this.m_count != _o.getCount()) {
                return false;
            } else if (this.m_daemonCount != _o.getDaemonCount()) {
                return false;
            } else if (this.m_peekCount != _o.getPeekCount()) {
                return false;
            } else if (this.m_totalStartedCount != _o.getTotalStartedCount()) {
                return false;
            } else if (this.m_catThreadCount != _o.getCatThreadCount()) {
                return false;
            } else if (this.m_pigeonThreadCount != _o.getPigeonThreadCount()) {
                return false;
            } else if (this.m_httpThreadCount != _o.getHttpThreadCount()) {
                return false;
            } else {
                return this.equals(this.m_dump, _o.getDump());
            }
        } else {
            return false;
        }
    }

    public int getCatThreadCount() {
        return this.m_catThreadCount;
    }

    public int getCount() {
        return this.m_count;
    }

    public int getDaemonCount() {
        return this.m_daemonCount;
    }

    public String getDump() {
        return this.m_dump;
    }

    public int getHttpThreadCount() {
        return this.m_httpThreadCount;
    }

    public int getPeekCount() {
        return this.m_peekCount;
    }

    public int getPigeonThreadCount() {
        return this.m_pigeonThreadCount;
    }

    public int getTotalStartedCount() {
        return this.m_totalStartedCount;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + this.m_count;
        hash = hash * 31 + this.m_daemonCount;
        hash = hash * 31 + this.m_peekCount;
        hash = hash * 31 + this.m_totalStartedCount;
        hash = hash * 31 + this.m_catThreadCount;
        hash = hash * 31 + this.m_pigeonThreadCount;
        hash = hash * 31 + this.m_httpThreadCount;
        hash = hash * 31 + (this.m_dump == null ? 0 : this.m_dump.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(ThreadsInfo other) {
        this.m_count = other.getCount();
        this.m_daemonCount = other.getDaemonCount();
        this.m_peekCount = other.getPeekCount();
        this.m_totalStartedCount = other.getTotalStartedCount();
        this.m_catThreadCount = other.getCatThreadCount();
        this.m_pigeonThreadCount = other.getPigeonThreadCount();
        this.m_httpThreadCount = other.getHttpThreadCount();
    }

    public ThreadsInfo setCatThreadCount(int catThreadCount) {
        this.m_catThreadCount = catThreadCount;
        return this;
    }

    public ThreadsInfo setCount(int count) {
        this.m_count = count;
        return this;
    }

    public ThreadsInfo setDaemonCount(int daemonCount) {
        this.m_daemonCount = daemonCount;
        return this;
    }

    public ThreadsInfo setDump(String dump) {
        this.m_dump = dump;
        return this;
    }

    public ThreadsInfo setHttpThreadCount(int httpThreadCount) {
        this.m_httpThreadCount = httpThreadCount;
        return this;
    }

    public ThreadsInfo setPeekCount(int peekCount) {
        this.m_peekCount = peekCount;
        return this;
    }

    public ThreadsInfo setPigeonThreadCount(int pigeonThreadCount) {
        this.m_pigeonThreadCount = pigeonThreadCount;
        return this;
    }

    public ThreadsInfo setTotalStartedCount(int totalStartedCount) {
        this.m_totalStartedCount = totalStartedCount;
        return this;
    }
}
