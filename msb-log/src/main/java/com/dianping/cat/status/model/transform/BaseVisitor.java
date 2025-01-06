package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.IVisitor;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.ExtensionDetail;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;

public abstract class BaseVisitor implements IVisitor {

    public void visitDisk(DiskInfo disk) {
        disk.getDiskVolumes().forEach(this::visitDiskVolume);
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo diskVolume) {
    }

    @Override
    public void visitExtension(Extension extension) {
        extension.getDetails().forEach((k, v) -> this.visitExtensionDetail(v));
    }


    @Override
    public void visitExtensionDetail(ExtensionDetail extensionDetail) {
    }

    @Override
    public void visitGc(GcInfo gc) {
    }

    @Override
    public void visitMemory(MemoryInfo memory) {
        memory.getGcs().forEach(this::visitGc);
    }

    @Override
    public void visitMessage(MessageInfo message) {
    }

    @Override
    public void visitOs(OsInfo os) {
    }

    @Override
    public void visitRuntime(RuntimeInfo runtime) {
    }

    @Override
    public void visitStatus(StatusInfo status) {
        if (status.getRuntime() != null) {
            this.visitRuntime(status.getRuntime());
        }
        if (status.getOs() != null) {
            this.visitOs(status.getOs());
        }
        if (status.getDisk() != null) {
            this.visitDisk(status.getDisk());
        }
        if (status.getMemory() != null) {
            this.visitMemory(status.getMemory());
        }
        if (status.getThread() != null) {
            this.visitThread(status.getThread());
        }
        if (status.getMessage() != null) {
            this.visitMessage(status.getMessage());
        }
        status.getExtensions().forEach((s, extension) -> BaseVisitor.this.visitExtension(extension));
    }

    @Override
    public void visitThread(ThreadsInfo thread) {
    }
}
