package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class GcInfo extends BaseEntity<GcInfo> {
    private String m_name;
    private long m_count;
    private long m_time;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitGc(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GcInfo) {
            GcInfo _o = (GcInfo) obj;
            if (!this.equals(this.m_name, _o.getName())) {
                return false;
            } else if (this.m_count != _o.getCount()) {
                return false;
            } else {
                return this.m_time == _o.getTime();
            }
        } else {
            return false;
        }
    }

    public long getCount() {
        return this.m_count;
    }

    public String getName() {
        return this.m_name;
    }

    public long getTime() {
        return this.m_time;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_name == null ? 0 : this.m_name.hashCode());
        hash = hash * 31 + (int) (this.m_count ^ this.m_count >>> 32);
        hash = hash * 31 + (int) (this.m_time ^ this.m_time >>> 32);
        return hash;
    }

    @Override
    public void mergeAttributes(GcInfo other) {
        if (other.getName() != null) {
            this.m_name = other.getName();
        }
        this.m_count = other.getCount();
        this.m_time = other.getTime();
    }

    public GcInfo setCount(long count) {
        this.m_count = count;
        return this;
    }

    public GcInfo setName(String name) {
        this.m_name = name;
        return this;
    }

    public GcInfo setTime(long time) {
        this.m_time = time;
        return this;
    }
}
