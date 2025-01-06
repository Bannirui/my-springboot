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

public interface ILinker {
    boolean onDisk(StatusInfo parent, DiskInfo disk);

    boolean onDiskVolume(DiskInfo parent, DiskVolumeInfo diskVolume);

    boolean onExtension(StatusInfo parent, Extension extension);

    boolean onExtensionDetail(Extension parent, ExtensionDetail extensionDetail);

    boolean onGc(MemoryInfo parent, GcInfo gc);

    boolean onMemory(StatusInfo parent, MemoryInfo memory);

    boolean onMessage(StatusInfo parent, MessageInfo message);

    boolean onOs(StatusInfo parent, OsInfo os);

    boolean onRuntime(StatusInfo parent, RuntimeInfo runtime);

    boolean onThread(StatusInfo parent, ThreadsInfo thread);
}
