package com.dianping.cat.status.model;

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

public interface IVisitor {
    void visitDisk(DiskInfo var1);

    void visitDiskVolume(DiskVolumeInfo var1);

    void visitExtension(Extension var1);

    void visitExtensionDetail(ExtensionDetail var1);

    void visitGc(GcInfo var1);

    void visitMemory(MemoryInfo var1);

    void visitMessage(MessageInfo var1);

    void visitOs(OsInfo var1);

    void visitRuntime(RuntimeInfo var1);

    void visitStatus(StatusInfo var1);

    void visitThread(ThreadsInfo var1);
}
