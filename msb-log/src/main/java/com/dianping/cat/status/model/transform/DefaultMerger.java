package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.IEntity;
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
import java.util.Stack;

public class DefaultMerger implements IVisitor {
    private Stack<Object> m_objs = new Stack<>();
    private StatusInfo m_status;

    public DefaultMerger(StatusInfo status) {
        this.m_status = status;
        this.m_objs.push(status);
    }

    public StatusInfo getStatus() {
        return this.m_status;
    }

    protected Stack<Object> getObjects() {
        return this.m_objs;
    }

    public <T> void merge(IEntity<T> to, IEntity<T> from) {
        this.m_objs.push(to);
        from.accept(this);
        this.m_objs.pop();
    }

    protected void mergeDisk(DiskInfo to, DiskInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeDiskVolume(DiskVolumeInfo to, DiskVolumeInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeExtension(Extension to, Extension from) {
        to.mergeAttributes(from);
        to.setDescription(from.getDescription());
    }

    protected void mergeExtensionDetail(ExtensionDetail to, ExtensionDetail from) {
        to.mergeAttributes(from);
    }

    protected void mergeGc(GcInfo to, GcInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeMemory(MemoryInfo to, MemoryInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeMessage(MessageInfo to, MessageInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeOs(OsInfo to, OsInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeRuntime(RuntimeInfo to, RuntimeInfo from) {
        to.mergeAttributes(from);
        to.setUserDir(from.getUserDir());
        to.setJavaClasspath(from.getJavaClasspath());
    }

    protected void mergeStatus(StatusInfo to, StatusInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeThread(ThreadsInfo to, ThreadsInfo from) {
        to.mergeAttributes(from);
        to.setDump(from.getDump());
    }

    @Override
    public void visitDisk(DiskInfo from) {
        DiskInfo to = (DiskInfo) this.m_objs.peek();
        this.mergeDisk(to, from);
        this.visitDiskChildren(to, from);
    }

    protected void visitDiskChildren(DiskInfo to, DiskInfo from) {
        from.getDiskVolumes().forEach(source -> {
            DiskVolumeInfo target = to.findDiskVolume(source.getId());
            if (target == null) {
                target = new DiskVolumeInfo(source.getId());
                to.addDiskVolume(target);
            }
            this.m_objs.push(target);
            source.accept(this);
            this.m_objs.pop();
        });
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo from) {
        DiskVolumeInfo to = (DiskVolumeInfo) this.m_objs.peek();
        this.mergeDiskVolume(to, from);
        this.visitDiskVolumeChildren(to, from);
    }

    protected void visitDiskVolumeChildren(DiskVolumeInfo to, DiskVolumeInfo from) {
    }

    @Override
    public void visitExtension(Extension from) {
        Extension to = (Extension) this.m_objs.peek();
        this.mergeExtension(to, from);
        this.visitExtensionChildren(to, from);
    }

    protected void visitExtensionChildren(Extension to, Extension from) {
        from.getDetails().forEach((s, source) -> {
            ExtensionDetail target = to.findExtensionDetail(source.getId());
            if (target == null) {
                target = new ExtensionDetail(source.getId());
                to.addExtensionDetail(target);
            }
            this.m_objs.push(target);
            source.accept(this);
            this.m_objs.pop();
        });
    }

    @Override
    public void visitExtensionDetail(ExtensionDetail from) {
        ExtensionDetail to = (ExtensionDetail) this.m_objs.peek();
        this.mergeExtensionDetail(to, from);
        this.visitExtensionDetailChildren(to, from);
    }

    protected void visitExtensionDetailChildren(ExtensionDetail to, ExtensionDetail from) {
    }

    @Override
    public void visitGc(GcInfo from) {
        GcInfo to = (GcInfo) this.m_objs.peek();
        this.mergeGc(to, from);
        this.visitGcChildren(to, from);
    }

    protected void visitGcChildren(GcInfo to, GcInfo from) {
    }

    @Override
    public void visitMemory(MemoryInfo from) {
        MemoryInfo to = (MemoryInfo) this.m_objs.peek();
        this.mergeMemory(to, from);
        this.visitMemoryChildren(to, from);
    }

    protected void visitMemoryChildren(MemoryInfo to, MemoryInfo from) {
        from.getGcs().forEach((source) -> {
            GcInfo target = null;
            if (target == null) {
                target = new GcInfo();
                to.addGc(target);
            }
            this.m_objs.push(target);
            source.accept(this);
            this.m_objs.pop();
        });
    }

    @Override
    public void visitMessage(MessageInfo from) {
        MessageInfo to = (MessageInfo) this.m_objs.peek();
        this.mergeMessage(to, from);
        this.visitMessageChildren(to, from);
    }

    protected void visitMessageChildren(MessageInfo to, MessageInfo from) {
    }

    @Override
    public void visitOs(OsInfo from) {
        OsInfo to = (OsInfo) this.m_objs.peek();
        this.mergeOs(to, from);
        this.visitOsChildren(to, from);
    }

    protected void visitOsChildren(OsInfo to, OsInfo from) {
    }

    @Override
    public void visitRuntime(RuntimeInfo from) {
        RuntimeInfo to = (RuntimeInfo) this.m_objs.peek();
        this.mergeRuntime(to, from);
        this.visitRuntimeChildren(to, from);
    }

    protected void visitRuntimeChildren(RuntimeInfo to, RuntimeInfo from) {
    }

    @Override
    public void visitStatus(StatusInfo from) {
        StatusInfo to = (StatusInfo) this.m_objs.peek();
        this.mergeStatus(to, from);
        this.visitStatusChildren(to, from);
    }

    protected void visitStatusChildren(StatusInfo to, StatusInfo from) {
        if (from.getRuntime() != null) {
            RuntimeInfo target = to.getRuntime();
            if (target == null) {
                target = new RuntimeInfo();
                to.setRuntime(target);
            }
            this.m_objs.push(target);
            from.getRuntime().accept(this);
            this.m_objs.pop();
        }
        if (from.getOs() != null) {
            OsInfo target = to.getOs();
            if (target == null) {
                target = new OsInfo();
                to.setOs(target);
            }
            this.m_objs.push(target);
            from.getOs().accept(this);
            this.m_objs.pop();
        }
        if (from.getDisk() != null) {
            DiskInfo target = to.getDisk();
            if (target == null) {
                target = new DiskInfo();
                to.setDisk(target);
            }
            this.m_objs.push(target);
            from.getDisk().accept(this);
            this.m_objs.pop();
        }
        if (from.getMemory() != null) {
            MemoryInfo target = to.getMemory();
            if (target == null) {
                target = new MemoryInfo();
                to.setMemory(target);
            }
            this.m_objs.push(target);
            from.getMemory().accept(this);
            this.m_objs.pop();
        }
        if (from.getThread() != null) {
            ThreadsInfo target = to.getThread();
            if (target == null) {
                target = new ThreadsInfo();
                to.setThread(target);
            }
            this.m_objs.push(target);
            from.getThread().accept(this);
            this.m_objs.pop();
        }
        if (from.getMessage() != null) {
            MessageInfo target = to.getMessage();
            if (target == null) {
                target = new MessageInfo();
                to.setMessage(target);
            }
            this.m_objs.push(target);
            from.getMessage().accept(this);
            this.m_objs.pop();
        }
        from.getExtensions().forEach((s, source) -> {
            Extension target = to.findExtension(source.getId());
            if (target == null) {
                target = new Extension(source.getId());
                to.addExtension(target);
            }
            this.m_objs.push(target);
            source.accept(this);
            this.m_objs.pop();
        });
    }

    @Override
    public void visitThread(ThreadsInfo from) {
        ThreadsInfo to = (ThreadsInfo) this.m_objs.peek();
        this.mergeThread(to, from);
        this.visitThreadChildren(to, from);
    }

    protected void visitThreadChildren(ThreadsInfo to, ThreadsInfo from) {
    }
}
