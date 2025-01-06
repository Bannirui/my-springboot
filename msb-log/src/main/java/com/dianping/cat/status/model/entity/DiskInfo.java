package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;
import java.util.ArrayList;
import java.util.List;

public class DiskInfo extends BaseEntity<DiskInfo> {
    private List<DiskVolumeInfo> m_diskVolumes = new ArrayList<>();

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDisk(this);
    }

    public DiskInfo addDiskVolume(DiskVolumeInfo diskVolume) {
        this.m_diskVolumes.add(diskVolume);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiskInfo) {
            DiskInfo _o = (DiskInfo) obj;
            return this.equals(this.m_diskVolumes, _o.getDiskVolumes());
        } else {
            return false;
        }
    }

    public DiskVolumeInfo findDiskVolume(String id) {
        for (DiskVolumeInfo v : this.m_diskVolumes) {
            if (this.equals(v.getId(), id)) {
                return v;
            }
        }
        return null;
    }

    public List<DiskVolumeInfo> getDiskVolumes() {
        return this.m_diskVolumes;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_diskVolumes == null ? 0 : this.m_diskVolumes.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(DiskInfo other) {
    }

    public DiskVolumeInfo removeDiskVolume(String id) {
        int len = this.m_diskVolumes.size();
        for (int i = 0; i < len; ++i) {
            DiskVolumeInfo diskVolume = (DiskVolumeInfo) this.m_diskVolumes.get(i);
            if (this.equals(diskVolume.getId(), id)) {
                return (DiskVolumeInfo) this.m_diskVolumes.remove(i);
            }
        }
        return null;
    }
}
