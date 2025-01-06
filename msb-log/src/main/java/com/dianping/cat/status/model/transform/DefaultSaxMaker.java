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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.xml.sax.Attributes;

public class DefaultSaxMaker implements IMaker<Attributes> {

    @Override
    public DiskInfo buildDisk(Attributes attributes) {
        return new DiskInfo();
    }

    @Override
    public DiskVolumeInfo buildDiskVolume(Attributes attributes) {
        String id = attributes.getValue("id");
        String total = attributes.getValue("total");
        String free = attributes.getValue("free");
        String usable = attributes.getValue("usable");
        DiskVolumeInfo diskVolume = new DiskVolumeInfo(id);
        if (total != null) {
            diskVolume.setTotal((Long) this.convert(Long.class, total, 0L));
        }
        if (free != null) {
            diskVolume.setFree((Long) this.convert(Long.class, free, 0L));
        }
        if (usable != null) {
            diskVolume.setUsable((Long) this.convert(Long.class, usable, 0L));
        }
        return diskVolume;
    }

    @Override
    public Extension buildExtension(Attributes attributes) {
        String id = attributes.getValue("id");
        Extension extension = new Extension(id);
        Map<String, String> dynamicAttributes = extension.getDynamicAttributes();
        int _length = attributes == null ? 0 : attributes.getLength();
        for (int i = 0; i < _length; ++i) {
            String _name = attributes.getQName(i);
            String _value = attributes.getValue(i);
            dynamicAttributes.put(_name, _value);
        }
        dynamicAttributes.remove("id");
        return extension;
    }

    @Override
    public ExtensionDetail buildExtensionDetail(Attributes attributes) {
        String id = attributes.getValue("id");
        String value = attributes.getValue("value");
        ExtensionDetail extensionDetail = new ExtensionDetail(id);
        if (value != null) {
            extensionDetail.setValue(this.convert(Double.class, value, 0.0D));
        }
        Map<String, String> dynamicAttributes = extensionDetail.getDynamicAttributes();
        int _length = attributes == null ? 0 : attributes.getLength();
        for (int i = 0; i < _length; ++i) {
            String _name = attributes.getQName(i);
            String _value = attributes.getValue(i);
            dynamicAttributes.put(_name, _value);
        }
        dynamicAttributes.remove("id");
        dynamicAttributes.remove("value");
        return extensionDetail;
    }

    @Override
    public GcInfo buildGc(Attributes attributes) {
        String name = attributes.getValue("name");
        String count = attributes.getValue("count");
        String time = attributes.getValue("time");
        GcInfo gc = new GcInfo();
        if (name != null) {
            gc.setName(name);
        }
        if (count != null) {
            gc.setCount((Long) this.convert(Long.class, count, 0L));
        }
        if (time != null) {
            gc.setTime((Long) this.convert(Long.class, time, 0L));
        }
        return gc;
    }

    @Override
    public MemoryInfo buildMemory(Attributes attributes) {
        String max = attributes.getValue("max");
        String total = attributes.getValue("total");
        String free = attributes.getValue("free");
        String heapUsage = attributes.getValue("heap-usage");
        String nonHeapUsage = attributes.getValue("non-heap-usage");
        MemoryInfo memory = new MemoryInfo();
        if (max != null) {
            memory.setMax((Long) this.convert(Long.class, max, 0L));
        }
        if (total != null) {
            memory.setTotal((Long) this.convert(Long.class, total, 0L));
        }
        if (free != null) {
            memory.setFree((Long) this.convert(Long.class, free, 0L));
        }
        if (heapUsage != null) {
            memory.setHeapUsage((Long) this.convert(Long.class, heapUsage, 0L));
        }
        if (nonHeapUsage != null) {
            memory.setNonHeapUsage((Long) this.convert(Long.class, nonHeapUsage, 0L));
        }
        return memory;
    }

    @Override
    public MessageInfo buildMessage(Attributes attributes) {
        String produced = attributes.getValue("produced");
        String overflowed = attributes.getValue("overflowed");
        String bytes = attributes.getValue("bytes");
        MessageInfo message = new MessageInfo();
        if (produced != null) {
            message.setProduced((Long) this.convert(Long.class, produced, 0L));
        }
        if (overflowed != null) {
            message.setOverflowed((Long) this.convert(Long.class, overflowed, 0L));
        }
        if (bytes != null) {
            message.setBytes((Long) this.convert(Long.class, bytes, 0L));
        }
        return message;
    }

    @Override
    public OsInfo buildOs(Attributes attributes) {
        String name = attributes.getValue("name");
        String arch = attributes.getValue("arch");
        String version = attributes.getValue("version");
        String availableProcessors = attributes.getValue("available-processors");
        String systemLoadAverage = attributes.getValue("system-load-average");
        String processTime = attributes.getValue("process-time");
        String totalPhysicalMemory = attributes.getValue("total-physical-memory");
        String freePhysicalMemory = attributes.getValue("free-physical-memory");
        String committedVirtualMemory = attributes.getValue("committed-virtual-memory");
        String totalSwapSpace = attributes.getValue("total-swap-space");
        String freeSwapSpace = attributes.getValue("free-swap-space");
        OsInfo os = new OsInfo();
        if (name != null) {
            os.setName(name);
        }
        if (arch != null) {
            os.setArch(arch);
        }
        if (version != null) {
            os.setVersion(version);
        }
        if (availableProcessors != null) {
            os.setAvailableProcessors((Integer) this.convert(Integer.class, availableProcessors, 0));
        }
        if (systemLoadAverage != null) {
            os.setSystemLoadAverage((Double) this.convert(Double.class, systemLoadAverage, 0.0D));
        }
        if (processTime != null) {
            os.setProcessTime((Long) this.convert(Long.class, processTime, 0L));
        }
        if (totalPhysicalMemory != null) {
            os.setTotalPhysicalMemory((Long) this.convert(Long.class, totalPhysicalMemory, 0L));
        }
        if (freePhysicalMemory != null) {
            os.setFreePhysicalMemory((Long) this.convert(Long.class, freePhysicalMemory, 0L));
        }
        if (committedVirtualMemory != null) {
            os.setCommittedVirtualMemory((Long) this.convert(Long.class, committedVirtualMemory, 0L));
        }
        if (totalSwapSpace != null) {
            os.setTotalSwapSpace((Long) this.convert(Long.class, totalSwapSpace, 0L));
        }
        if (freeSwapSpace != null) {
            os.setFreeSwapSpace((Long) this.convert(Long.class, freeSwapSpace, 0L));
        }
        return os;
    }

    @Override
    public RuntimeInfo buildRuntime(Attributes attributes) {
        String startTime = attributes.getValue("start-time");
        String upTime = attributes.getValue("up-time");
        String javaVersion = attributes.getValue("java-version");
        String userName = attributes.getValue("user-name");
        RuntimeInfo runtime = new RuntimeInfo();
        if (startTime != null) {
            runtime.setStartTime((Long) this.convert(Long.class, startTime, 0L));
        }
        if (upTime != null) {
            runtime.setUpTime((Long) this.convert(Long.class, upTime, 0L));
        }
        if (javaVersion != null) {
            runtime.setJavaVersion(javaVersion);
        }
        if (userName != null) {
            runtime.setUserName(userName);
        }
        return runtime;
    }

    @Override
    public StatusInfo buildStatus(Attributes attributes) {
        String timestamp = attributes.getValue("timestamp");
        StatusInfo status = new StatusInfo();
        if (timestamp != null) {
            status.setTimestamp(this.toDate(timestamp, "yyyy-MM-dd HH:mm:ss.SSS", (Date) null));
        }
        return status;
    }

    @Override
    public ThreadsInfo buildThread(Attributes attributes) {
        String count = attributes.getValue("count");
        String daemonCount = attributes.getValue("daemon-count");
        String peekCount = attributes.getValue("peek-count");
        String totalStartedCount = attributes.getValue("total-started-count");
        String catThreadCount = attributes.getValue("cat-thread-count");
        String pigeonThreadCount = attributes.getValue("pigeon-thread-count");
        String httpThreadCount = attributes.getValue("http-thread-count");
        ThreadsInfo thread = new ThreadsInfo();
        if (count != null) {
            thread.setCount((Integer) this.convert(Integer.class, count, 0));
        }
        if (daemonCount != null) {
            thread.setDaemonCount((Integer) this.convert(Integer.class, daemonCount, 0));
        }
        if (peekCount != null) {
            thread.setPeekCount((Integer) this.convert(Integer.class, peekCount, 0));
        }
        if (totalStartedCount != null) {
            thread.setTotalStartedCount((Integer) this.convert(Integer.class, totalStartedCount, 0));
        }
        if (catThreadCount != null) {
            thread.setCatThreadCount((Integer) this.convert(Integer.class, catThreadCount, 0));
        }
        if (pigeonThreadCount != null) {
            thread.setPigeonThreadCount((Integer) this.convert(Integer.class, pigeonThreadCount, 0));
        }
        if (httpThreadCount != null) {
            thread.setHttpThreadCount((Integer) this.convert(Integer.class, httpThreadCount, 0));
        }
        return thread;
    }

    protected <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Short.class) {
            return (T) Short.valueOf(value);
        } else if (type == Float.class) {
            return (T) Float.valueOf(value);
        } else if (type == Double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if (type == Character.class) {
            return (T) (Character) value.charAt(0);
        } else {
            return (T) value;
        }
    }

    protected Date toDate(String str, String format, Date defaultValue) {
        if (str != null && str.length() != 0) {
            try {
                return (new SimpleDateFormat(format)).parse(str);
            } catch (ParseException e) {
                throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
            }
        } else {
            return defaultValue;
        }
    }
}
