package com.dianping.cat.status.model.transform;

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
import java.util.ArrayList;
import java.util.List;

public class DefaultLinker implements ILinker {
    private boolean m_deferrable;
    private List<Runnable> m_deferedJobs = new ArrayList<>();

    public DefaultLinker(boolean deferrable) {
        this.m_deferrable = deferrable;
    }

    public void finish() {
        this.m_deferedJobs.forEach(Runnable::run);
    }

    @Override
    public boolean onDisk(StatusInfo parent, DiskInfo disk) {
        parent.setDisk(disk);
        return true;
    }

    @Override
    public boolean onDiskVolume(DiskInfo parent, DiskVolumeInfo diskVolume) {
        parent.addDiskVolume(diskVolume);
        return true;
    }

    @Override
    public boolean onExtension(final StatusInfo parent, final Extension extension) {
        if (this.m_deferrable) {
            this.m_deferedJobs.add(() -> parent.addExtension(extension));
        } else {
            parent.addExtension(extension);
        }
        return true;
    }

    @Override
    public boolean onExtensionDetail(final Extension parent, final ExtensionDetail extensionDetail) {
        if (this.m_deferrable) {
            this.m_deferedJobs.add(() -> parent.addExtensionDetail(extensionDetail));
        } else {
            parent.addExtensionDetail(extensionDetail);
        }
        return true;
    }

    @Override
    public boolean onGc(MemoryInfo parent, GcInfo gc) {
        parent.addGc(gc);
        return true;
    }

    @Override
    public boolean onMemory(StatusInfo parent, MemoryInfo memory) {
        parent.setMemory(memory);
        return true;
    }

    @Override
    public boolean onMessage(StatusInfo parent, MessageInfo message) {
        parent.setMessage(message);
        return true;
    }

    @Override
    public boolean onOs(StatusInfo parent, OsInfo os) {
        parent.setOs(os);
        return true;
    }

    @Override
    public boolean onRuntime(StatusInfo parent, RuntimeInfo runtime) {
        parent.setRuntime(runtime);
        return true;
    }

    @Override
    public boolean onThread(StatusInfo parent, ThreadsInfo thread) {
        parent.setThread(thread);
        return true;
    }
}
