package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;
import java.util.ArrayList;
import java.util.List;

public class MemoryInfo extends BaseEntity<MemoryInfo> {
    private long m_max;
    private long m_total;
    private long m_free;
    private long m_heapUsage;
    private long m_nonHeapUsage;
    private List<GcInfo> m_gcs = new ArrayList<>();

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitMemory(this);
    }

    public MemoryInfo addGc(GcInfo gc) {
        this.m_gcs.add(gc);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemoryInfo) {
            MemoryInfo _o = (MemoryInfo) obj;
            if (this.m_max != _o.getMax()) {
                return false;
            } else if (this.m_total != _o.getTotal()) {
                return false;
            } else if (this.m_free != _o.getFree()) {
                return false;
            } else if (this.m_heapUsage != _o.getHeapUsage()) {
                return false;
            } else if (this.m_nonHeapUsage != _o.getNonHeapUsage()) {
                return false;
            } else {
                return this.equals(this.m_gcs, _o.getGcs());
            }
        } else {
            return false;
        }
    }

    public long getFree() {
        return this.m_free;
    }

    public List<GcInfo> getGcs() {
        return this.m_gcs;
    }

    public long getHeapUsage() {
        return this.m_heapUsage;
    }

    public long getMax() {
        return this.m_max;
    }

    public long getNonHeapUsage() {
        return this.m_nonHeapUsage;
    }

    public long getTotal() {
        return this.m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (int) (this.m_max ^ this.m_max >>> 32);
        hash = hash * 31 + (int) (this.m_total ^ this.m_total >>> 32);
        hash = hash * 31 + (int) (this.m_free ^ this.m_free >>> 32);
        hash = hash * 31 + (int) (this.m_heapUsage ^ this.m_heapUsage >>> 32);
        hash = hash * 31 + (int) (this.m_nonHeapUsage ^ this.m_nonHeapUsage >>> 32);
        hash = hash * 31 + (this.m_gcs == null ? 0 : this.m_gcs.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(MemoryInfo other) {
        this.m_max = other.getMax();
        this.m_total = other.getTotal();
        this.m_free = other.getFree();
        this.m_heapUsage = other.getHeapUsage();
        this.m_nonHeapUsage = other.getNonHeapUsage();
    }

    public MemoryInfo setFree(long free) {
        this.m_free = free;
        return this;
    }

    public MemoryInfo setHeapUsage(long heapUsage) {
        this.m_heapUsage = heapUsage;
        return this;
    }

    public MemoryInfo setMax(long max) {
        this.m_max = max;
        return this;
    }

    public MemoryInfo setNonHeapUsage(long nonHeapUsage) {
        this.m_nonHeapUsage = nonHeapUsage;
        return this;
    }

    public MemoryInfo setTotal(long total) {
        this.m_total = total;
        return this;
    }
}
