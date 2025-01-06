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

public interface IParser<T> {
    StatusInfo parse(IMaker<T> maker, ILinker linker, T t);

    void parseForDiskInfo(IMaker<T> maker, ILinker linker, DiskInfo disk, T t);

    void parseForDiskVolumeInfo(IMaker<T> maker, ILinker linker, DiskVolumeInfo diskVolume, T t);

    void parseForExtension(IMaker<T> maker, ILinker linker, Extension extension, T t);

    void parseForExtensionDetail(IMaker<T> maker, ILinker linker, ExtensionDetail extensionDetail, T t);

    void parseForGcInfo(IMaker<T> maker, ILinker linker, GcInfo gc, T t);

    void parseForMemoryInfo(IMaker<T> maker, ILinker linker, MemoryInfo memory, T t);

    void parseForMessageInfo(IMaker<T> maker, ILinker linker, MessageInfo message, T t);

    void parseForOsInfo(IMaker<T> maker, ILinker linker, OsInfo os, T t);

    void parseForRuntimeInfo(IMaker<T> maker, ILinker linker, RuntimeInfo runtime, T t);

    void parseForThreadsInfo(IMaker<T> maker, ILinker linker, ThreadsInfo thread, T t);
}
