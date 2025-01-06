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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DefaultXmlBuilder implements IVisitor {
    private IVisitor m_visitor;
    private int m_level;
    private StringBuilder m_sb;
    private boolean m_compact;

    public DefaultXmlBuilder() {
        this(false);
    }

    public DefaultXmlBuilder(boolean compact) {
        this(compact, new StringBuilder(4096));
    }

    public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
        this.m_visitor = this;
        this.m_compact = compact;
        this.m_sb = sb;
        this.m_sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
    }

    public String buildXml(IEntity<?> entity) {
        entity.accept(this.m_visitor);
        return this.m_sb.toString();
    }

    protected void endTag(String name) {
        --this.m_level;
        this.indent();
        this.m_sb.append("</").append(name).append(">\r\n");
    }

    protected String escape(Object value) {
        return this.escape(value, false);
    }

    protected String escape(Object value, boolean text) {
        if (value == null) {
            return null;
        } else {
            String str = value.toString();
            int len = str.length();
            StringBuilder sb = new StringBuilder(len + 16);

            for (int i = 0; i < len; ++i) {
                char ch = str.charAt(i);
                switch (ch) {
                    case '"':
                        if (!text) {
                            sb.append("&quot;");
                        } else {
                            sb.append(ch);
                        }
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    default:
                        sb.append(ch);
                        break;
                }
            }
            return sb.toString();
        }
    }

    protected void indent() {
        if (!this.m_compact) {
            for (int i = this.m_level - 1; i >= 0; --i) {
                this.m_sb.append("   ");
            }
        }
    }

    protected void startTag(String name) {
        this.startTag(name, false, null);
    }

    protected void startTag(String name, boolean closed, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.startTag(name, null, closed, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.startTag(name, null, false, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Object text, boolean closed, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.indent();
        this.m_sb.append('<').append(name);
        int len = nameValues.length;
        for (int i = 0; i + 1 < len; i += 2) {
            Object attrName = nameValues[i];
            Object attrValue = nameValues[i + 1];
            if (attrValue != null) {
                this.m_sb.append(' ').append(attrName).append("=\"").append(this.escape(attrValue)).append('"');
            }
        }
        if (dynamicAttributes != null) {
            dynamicAttributes.forEach((k, v) -> {
                this.m_sb.append(' ').append(k).append("=\"").append(this.escape(v)).append('"');
            });
        }
        if (text != null && closed) {
            this.m_sb.append('>');
            this.m_sb.append(this.escape(text, true));
            this.m_sb.append("</").append(name).append(">\r\n");
        } else {
            if (closed) {
                this.m_sb.append('/');
            } else {
                ++this.m_level;
            }
            this.m_sb.append(">\r\n");
        }
    }

    protected void tagWithText(String name, String text, Object... nameValues) {
        if (text != null) {
            this.indent();
            this.m_sb.append('<').append(name);
            int len = nameValues.length;
            for (int i = 0; i + 1 < len; i += 2) {
                Object attrName = nameValues[i];
                Object attrValue = nameValues[i + 1];
                if (attrValue != null) {
                    this.m_sb.append(' ').append(attrName).append("=\"").append(this.escape(attrValue)).append('"');
                }
            }
            this.m_sb.append(">");
            this.m_sb.append(this.escape(text, true));
            this.m_sb.append("</").append(name).append(">\r\n");
        }
    }

    protected void element(String name, String text, boolean escape) {
        if (text != null) {
            this.indent();
            this.m_sb.append('<').append(name).append(">");
            if (escape) {
                this.m_sb.append(this.escape(text, true));
            } else {
                this.m_sb.append("<![CDATA[").append(text).append("]]>");
            }
            this.m_sb.append("</").append(name).append(">\r\n");
        }
    }

    protected String toString(Date date, String format) {
        return date != null ? (new SimpleDateFormat(format)).format(date) : null;
    }

    @Override
    public void visitDisk(DiskInfo disk) {
        this.startTag("disk", null);
        if (!disk.getDiskVolumes().isEmpty()) {
            disk.getDiskVolumes().forEach(diskVolume -> diskVolume.accept(this.m_visitor));
        }
        this.endTag("disk");
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo diskVolume) {
        this.startTag("disk-volume", true, null, "id", diskVolume.getId(), "total", diskVolume.getTotal(), "free", diskVolume.getFree(),
            "usable", diskVolume.getUsable());
    }

    @Override
    public void visitExtension(Extension extension) {
        this.startTag("extension", extension.getDynamicAttributes(), "id", extension.getId());
        this.element("description", extension.getDescription(), false);
        if (!extension.getDetails().isEmpty()) {
            extension.getDetails().forEach((s, v) -> v.accept(this.m_visitor));
        }
        this.endTag("extension");
    }

    @Override
    public void visitExtensionDetail(ExtensionDetail extensionDetail) {
        this.startTag("extensionDetail", true, extensionDetail.getDynamicAttributes(), "id", extensionDetail.getId(), "value",
            extensionDetail.getValue());
    }

    @Override
    public void visitGc(GcInfo gc) {
        this.startTag("gc", true, null, "name", gc.getName(), "count", gc.getCount(), "time", gc.getTime());
    }

    @Override
    public void visitMemory(MemoryInfo memory) {
        this.startTag("memory", null, "max", memory.getMax(), "total", memory.getTotal(), "free", memory.getFree(), "heap-usage",
            memory.getHeapUsage(), "non-heap-usage", memory.getNonHeapUsage());
        if (!memory.getGcs().isEmpty()) {
            memory.getGcs().forEach(gc -> gc.accept(this.m_visitor));
        }
        this.endTag("memory");
    }

    @Override
    public void visitMessage(MessageInfo message) {
        this.startTag("message", true, null, "produced", message.getProduced(), "overflowed", message.getOverflowed(), "bytes",
            message.getBytes());
    }

    @Override
    public void visitOs(OsInfo os) {
        this.startTag("os", true, null, "name", os.getName(), "arch", os.getArch(), "version", os.getVersion(), "available-processors",
            os.getAvailableProcessors(), "system-load-average", os.getSystemLoadAverage(), "process-time", os.getProcessTime(), "process-up-time",
            os.getProcessUpTime(), "system-cpu-usage", os.getSystemCpuUsage(), "total-physical-memory", os.getTotalPhysicalMemory(),
            "free-physical-memory", os.getFreePhysicalMemory(), "committed-virtual-memory", os.getCommittedVirtualMemory(), "total-swap-space",
            os.getTotalSwapSpace(), "free-swap-space", os.getFreeSwapSpace());
    }

    @Override
    public void visitRuntime(RuntimeInfo runtime) {
        this.startTag("runtime", null, "start-time", runtime.getStartTime(), "up-time", runtime.getUpTime(), "java-version",
            runtime.getJavaVersion(), "user-name", runtime.getUserName());
        this.element("user-dir", runtime.getUserDir(), true);
        this.element("java-classpath", runtime.getJavaClasspath(), true);
        this.endTag("runtime");
    }

    @Override
    public void visitStatus(StatusInfo status) {
        this.startTag("status", null, "timestamp", this.toString(status.getTimestamp(), "yyyy-MM-dd HH:mm:ss.SSS"));
        if (status.getRuntime() != null) {
            status.getRuntime().accept(this.m_visitor);
        }
        if (status.getOs() != null) {
            status.getOs().accept(this.m_visitor);
        }
        if (status.getDisk() != null) {
            status.getDisk().accept(this.m_visitor);
        }
        if (status.getMemory() != null) {
            status.getMemory().accept(this.m_visitor);
        }
        if (status.getThread() != null) {
            status.getThread().accept(this.m_visitor);
        }
        if (status.getMessage() != null) {
            status.getMessage().accept(this.m_visitor);
        }
        if (!status.getExtensions().isEmpty()) {
            status.getExtensions().forEach((s, v) -> v.accept(this.m_visitor));
        }
        this.endTag("status");
    }

    @Override
    public void visitThread(ThreadsInfo thread) {
        this.startTag("thread", null, "count", thread.getCount(), "daemon-count", thread.getDaemonCount(), "peek-count", thread.getPeekCount(),
            "total-started-count", thread.getTotalStartedCount(), "cat-thread-count", thread.getCatThreadCount(), "pigeon-thread-count",
            thread.getPigeonThreadCount(), "http-thread-count", thread.getHttpThreadCount());
        this.element("dump", thread.getDump(), true);
        this.endTag("thread");
    }
}
