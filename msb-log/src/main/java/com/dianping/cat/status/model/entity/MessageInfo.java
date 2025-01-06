package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class MessageInfo extends BaseEntity<MessageInfo> {
    private long m_produced;
    private long m_overflowed;
    private long m_bytes;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitMessage(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageInfo) {
            MessageInfo _o = (MessageInfo) obj;
            if (this.m_produced != _o.getProduced()) {
                return false;
            } else if (this.m_overflowed != _o.getOverflowed()) {
                return false;
            } else {
                return this.m_bytes == _o.getBytes();
            }
        } else {
            return false;
        }
    }

    public long getBytes() {
        return this.m_bytes;
    }

    public long getOverflowed() {
        return this.m_overflowed;
    }

    public long getProduced() {
        return this.m_produced;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (int) (this.m_produced ^ this.m_produced >>> 32);
        hash = hash * 31 + (int) (this.m_overflowed ^ this.m_overflowed >>> 32);
        hash = hash * 31 + (int) (this.m_bytes ^ this.m_bytes >>> 32);
        return hash;
    }

    @Override
    public void mergeAttributes(MessageInfo other) {
        this.m_produced = other.getProduced();
        this.m_overflowed = other.getOverflowed();
        this.m_bytes = other.getBytes();
    }

    public MessageInfo setBytes(long bytes) {
        this.m_bytes = bytes;
        return this;
    }

    public MessageInfo setOverflowed(long overflowed) {
        this.m_overflowed = overflowed;
        return this;
    }

    public MessageInfo setProduced(long produced) {
        this.m_produced = produced;
        return this;
    }
}
