package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatusInfo extends BaseEntity<StatusInfo> {
    private Date m_timestamp;
    private RuntimeInfo m_runtime;
    private OsInfo m_os;
    private DiskInfo m_disk;
    private MemoryInfo m_memory;
    private ThreadsInfo m_thread;
    private MessageInfo m_message;
    private Map<String, Extension> m_extensions = new LinkedHashMap<>();

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitStatus(this);
    }

    public StatusInfo addExtension(Extension extension) {
        this.m_extensions.put(extension.getId(), extension);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatusInfo) {
            StatusInfo _o = (StatusInfo) obj;
            if (!this.equals(this.m_timestamp, _o.getTimestamp())) {
                return false;
            } else if (!this.equals(this.m_runtime, _o.getRuntime())) {
                return false;
            } else if (!this.equals(this.m_os, _o.getOs())) {
                return false;
            } else if (!this.equals(this.m_disk, _o.getDisk())) {
                return false;
            } else if (!this.equals(this.m_memory, _o.getMemory())) {
                return false;
            } else if (!this.equals(this.m_thread, _o.getThread())) {
                return false;
            } else if (!this.equals(this.m_message, _o.getMessage())) {
                return false;
            } else {
                return this.equals(this.m_extensions, _o.getExtensions());
            }
        } else {
            return false;
        }
    }

    public Extension findExtension(String id) {
        return this.m_extensions.get(id);
    }

    public Extension findOrCreateExtension(String id) {
        Extension extension = this.m_extensions.get(id);
        if (extension == null) {
            synchronized (this.m_extensions) {
                extension = this.m_extensions.get(id);
                if (extension == null) {
                    extension = new Extension(id);
                    this.m_extensions.put(id, extension);
                }
            }
        }
        return extension;
    }

    public DiskInfo getDisk() {
        return this.m_disk;
    }

    public Map<String, Extension> getExtensions() {
        return this.m_extensions;
    }

    public MemoryInfo getMemory() {
        return this.m_memory;
    }

    public MessageInfo getMessage() {
        return this.m_message;
    }

    public OsInfo getOs() {
        return this.m_os;
    }

    public RuntimeInfo getRuntime() {
        return this.m_runtime;
    }

    public ThreadsInfo getThread() {
        return this.m_thread;
    }

    public Date getTimestamp() {
        return this.m_timestamp;
    }

    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_timestamp == null ? 0 : this.m_timestamp.hashCode());
        hash = hash * 31 + (this.m_runtime == null ? 0 : this.m_runtime.hashCode());
        hash = hash * 31 + (this.m_os == null ? 0 : this.m_os.hashCode());
        hash = hash * 31 + (this.m_disk == null ? 0 : this.m_disk.hashCode());
        hash = hash * 31 + (this.m_memory == null ? 0 : this.m_memory.hashCode());
        hash = hash * 31 + (this.m_thread == null ? 0 : this.m_thread.hashCode());
        hash = hash * 31 + (this.m_message == null ? 0 : this.m_message.hashCode());
        hash = hash * 31 + (this.m_extensions == null ? 0 : this.m_extensions.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(StatusInfo other) {
        if (other.getTimestamp() != null) {
            this.m_timestamp = other.getTimestamp();
        }
    }

    public Extension removeExtension(String id) {
        return this.m_extensions.remove(id);
    }

    public StatusInfo setDisk(DiskInfo disk) {
        this.m_disk = disk;
        return this;
    }

    public StatusInfo setMemory(MemoryInfo memory) {
        this.m_memory = memory;
        return this;
    }

    public StatusInfo setMessage(MessageInfo message) {
        this.m_message = message;
        return this;
    }

    public StatusInfo setOs(OsInfo os) {
        this.m_os = os;
        return this;
    }

    public StatusInfo setRuntime(RuntimeInfo runtime) {
        this.m_runtime = runtime;
        return this;
    }

    public StatusInfo setThread(ThreadsInfo thread) {
        this.m_thread = thread;
        return this;
    }

    public StatusInfo setTimestamp(Date timestamp) {
        this.m_timestamp = timestamp;
        return this;
    }
}
