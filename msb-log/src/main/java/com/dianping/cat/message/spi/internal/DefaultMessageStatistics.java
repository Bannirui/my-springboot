package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageStatistics implements MessageStatistics {
    private long m_produced;
    private long m_overflowed;
    private long m_bytes;

    @Override
    public long getBytes() {
        return this.m_bytes;
    }

    @Override
    public long getOverflowed() {
        return this.m_overflowed;
    }

    @Override
    public long getProduced() {
        return this.m_produced;
    }

    @Override
    public void onBytes(int bytes) {
        this.m_bytes += bytes;
        ++this.m_produced;
    }

    @Override
    public void onOverflowed(MessageTree tree) {
        ++this.m_overflowed;
    }
}
