package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class DiskVolumeInfo extends BaseEntity<DiskVolumeInfo> {
    private String m_id;
    private long m_total;
    private long m_free;
    private long m_usable;

    public DiskVolumeInfo() {
    }

    public DiskVolumeInfo(String id) {
        this.m_id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDiskVolume(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiskVolumeInfo) {
            DiskVolumeInfo _o = (DiskVolumeInfo)obj;
            return this.equals(this.m_id, _o.getId());
        } else {
            return false;
        }
    }

    public long getFree() {
        return this.m_free;
    }

    public String getId() {
        return this.m_id;
    }

    public long getTotal() {
        return this.m_total;
    }

    public long getUsable() {
        return this.m_usable;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_id == null ? 0 : this.m_id.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(DiskVolumeInfo other) {
        this.assertAttributeEquals(other, "disk-volume", "id", this.m_id, other.getId());
        this.m_total = other.getTotal();
        this.m_free = other.getFree();
        this.m_usable = other.getUsable();
    }

    public DiskVolumeInfo setFree(long free) {
        this.m_free = free;
        return this;
    }

    public DiskVolumeInfo setId(String id) {
        this.m_id = id;
        return this;
    }

    public DiskVolumeInfo setTotal(long total) {
        this.m_total = total;
        return this;
    }

    public DiskVolumeInfo setUsable(long usable) {
        this.m_usable = usable;
        return this;
    }
}
