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

public interface IMaker<T> {
    DiskInfo buildDisk(T t);

    DiskVolumeInfo buildDiskVolume(T t);

    Extension buildExtension(T t);

    ExtensionDetail buildExtensionDetail(T t);

    GcInfo buildGc(T t);

    MemoryInfo buildMemory(T t);

    MessageInfo buildMessage(T t);

    OsInfo buildOs(T t);

    RuntimeInfo buildRuntime(T t);

    StatusInfo buildStatus(T t);

    ThreadsInfo buildThread(T t);
}
