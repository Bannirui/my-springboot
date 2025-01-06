package com.dianping.cat.status;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;
import com.dianping.cat.status.model.transform.BaseVisitor;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class StatusInfoCollector extends BaseVisitor {
    private MessageStatistics m_statistics;
    private boolean m_dumpLocked;
    private String m_jars;
    private String m_dataPath = "/data";
    private StatusInfo m_statusInfo;

    public StatusInfoCollector(MessageStatistics statistics, String jars) {
        this.m_statistics = statistics;
        this.m_jars = jars;
    }

    private int countThreadsByPrefix(ThreadInfo[] threads, String... prefixes) {
        int count = 0;
        for (ThreadInfo thread : threads) {
            for (String prefix : prefixes) {
                if (thread.getThreadName().startsWith(prefix)) {
                    ++count;
                }
            }
        }
        return count;
    }

    private int countThreadsBySubstring(ThreadInfo[] threads, String... substrings) {
        int count = 0;
        for (ThreadInfo thread : threads) {
            for (String str : substrings) {
                if (thread.getThreadName().contains(str)) {
                    ++count;
                }
            }
        }
        return count;
    }

    private String getThreadDump(ThreadInfo[] threads) {
        StringBuilder sb = new StringBuilder(32768);
        int index = 1;
        TreeMap<String, ThreadInfo> sortedThreads = new TreeMap<>();
        for (ThreadInfo thread : threads) {
            sortedThreads.put(thread.getThreadName(), thread);
        }
        for (ThreadInfo thread : sortedThreads.values()) {
            sb.append(index++).append(": ").append(thread);
        }
        return sb.toString();
    }

    boolean isInstanceOfInterface(Class<?> clazz, String interfaceName) {
        if (clazz == Object.class) {
            return false;
        } else if (clazz.getName().equals(interfaceName)) {
            return true;
        } else {
            Class<?>[] interfaceclasses = clazz.getInterfaces();
            for (Class<?> interfaceClass : interfaceclasses) {
                if (this.isInstanceOfInterface(interfaceClass, interfaceName)) {
                    return true;
                }
            }
            return this.isInstanceOfInterface(clazz.getSuperclass(), interfaceName);
        }
    }

    public StatusInfoCollector setDumpLocked(boolean dumpLocked) {
        this.m_dumpLocked = dumpLocked;
        return this;
    }

    @Override
    public void visitDisk(DiskInfo disk) {
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                disk.addDiskVolume(new DiskVolumeInfo(root.getAbsolutePath()));
            }
        }
        File data = new File(this.m_dataPath);
        if (data.exists()) {
            disk.addDiskVolume(new DiskVolumeInfo(data.getAbsolutePath()));
        }
        super.visitDisk(disk);
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo diskVolume) {
        Extension diskExtension = this.m_statusInfo.findOrCreateExtension("Disk");
        File volume = new File(diskVolume.getId());
        diskVolume.setTotal(volume.getTotalSpace());
        diskVolume.setFree(volume.getFreeSpace());
        diskVolume.setUsable(volume.getUsableSpace());
        diskExtension.findOrCreateExtensionDetail(diskVolume.getId() + " Free").setValue((double) volume.getFreeSpace());
    }

    @Override
    public void visitMemory(MemoryInfo memory) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();
        memory.setMax(runtime.maxMemory());
        memory.setTotal(runtime.totalMemory());
        memory.setFree(runtime.freeMemory());
        memory.setHeapUsage(bean.getHeapMemoryUsage().getUsed());
        memory.setNonHeapUsage(bean.getNonHeapMemoryUsage().getUsed());
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        Extension gcExtension = this.m_statusInfo.findOrCreateExtension("GC");
        for (GarbageCollectorMXBean mxbean : beans) {
            if (mxbean.isValid()) {
                GcInfo gc = new GcInfo();
                String name = mxbean.getName();
                long count = mxbean.getCollectionCount();
                gc.setName(name);
                gc.setCount(count);
                gc.setTime(mxbean.getCollectionTime());
                memory.addGc(gc);
                gcExtension.findOrCreateExtensionDetail(name + "Count").setValue((double) count);
                gcExtension.findOrCreateExtensionDetail(name + "Time").setValue((double) mxbean.getCollectionTime());
            }
        }
        Extension heapUsage = this.m_statusInfo.findOrCreateExtension("JVMHeap");
        ManagementFactory.getMemoryPoolMXBeans().forEach(x -> {
            long count = x.getUsage().getUsed();
            String name = x.getName();
            heapUsage.findOrCreateExtensionDetail(name).setValue((double) count);
        });
        super.visitMemory(memory);
    }

    @Override
    public void visitMessage(MessageInfo message) {
        Extension catExtension = this.m_statusInfo.findOrCreateExtension("CatUsage");
        if (this.m_statistics != null) {
            catExtension.findOrCreateExtensionDetail("Produced").setValue((double) this.m_statistics.getProduced());
            catExtension.findOrCreateExtensionDetail("Overflowed").setValue((double) this.m_statistics.getOverflowed());
            catExtension.findOrCreateExtensionDetail("Bytes").setValue((double) this.m_statistics.getBytes());
        }

    }

    @Override
    public void visitOs(OsInfo os) {
        Extension systemExtension = this.m_statusInfo.findOrCreateExtension("System");
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        os.setArch(bean.getArch());
        os.setName(bean.getName());
        os.setVersion(bean.getVersion());
        os.setAvailableProcessors(bean.getAvailableProcessors());
        os.setSystemLoadAverage(bean.getSystemLoadAverage());
        systemExtension.findOrCreateExtensionDetail("LoadAverage").setValue(bean.getSystemLoadAverage());
        if (this.isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
            com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;
            RuntimeMXBean runtimeBean = ManagementFactory.getPlatformMXBean(RuntimeMXBean.class);
            os.setTotalPhysicalMemory(b.getTotalPhysicalMemorySize());
            os.setFreePhysicalMemory(b.getFreePhysicalMemorySize());
            os.setTotalSwapSpace(b.getTotalSwapSpaceSize());
            os.setFreeSwapSpace(b.getFreeSwapSpaceSize());
            os.setProcessTime(b.getProcessCpuTime());
            os.setProcessUpTime(runtimeBean.getUptime());
            os.setSystemCpuUsage(b.getSystemCpuLoad());
            os.setCommittedVirtualMemory(b.getCommittedVirtualMemorySize());
            systemExtension.findOrCreateExtensionDetail("FreePhysicalMemory").setValue((double) b.getFreePhysicalMemorySize());
            systemExtension.findOrCreateExtensionDetail("FreeSwapSpaceSize").setValue((double) b.getFreeSwapSpaceSize());
        }
        this.m_statusInfo.addExtension(systemExtension);
    }

    @Override
    public void visitRuntime(RuntimeInfo runtime) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        runtime.setStartTime(bean.getStartTime());
        runtime.setUpTime(bean.getUptime());
        runtime.setJavaClasspath(this.m_jars);
        runtime.setJavaVersion(System.getProperty("java.version"));
        runtime.setUserDir(System.getProperty("user.dir"));
        runtime.setUserName(System.getProperty("user.name"));
    }

    @Override
    public void visitStatus(StatusInfo status) {
        status.setTimestamp(new Date());
        status.setOs(new OsInfo());
        status.setDisk(new DiskInfo());
        status.setRuntime(new RuntimeInfo());
        status.setMemory(new MemoryInfo());
        status.setThread(new ThreadsInfo());
        status.setMessage(new MessageInfo());
        this.m_statusInfo = status;
        super.visitStatus(status);
    }

    @Override
    public void visitThread(ThreadsInfo thread) {
        Extension frameworkThread = this.m_statusInfo.findOrCreateExtension("FrameworkThread");
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        bean.setThreadContentionMonitoringEnabled(true);
        ThreadInfo[] threads;
        if (this.m_dumpLocked) {
            threads = bean.dumpAllThreads(true, true);
        } else {
            threads = bean.dumpAllThreads(false, false);
        }
        thread.setCount(bean.getThreadCount());
        thread.setDaemonCount(bean.getDaemonThreadCount());
        thread.setPeekCount(bean.getPeakThreadCount());
        thread.setTotalStartedCount((int) bean.getTotalStartedThreadCount());
        int jbossThreadsCount = this.countThreadsByPrefix(threads, "http-", "catalina-exec-");
        int jettyThreadsCount = this.countThreadsBySubstring(threads, "@qtp");
        thread.setDump(this.getThreadDump(threads));
        frameworkThread.findOrCreateExtensionDetail("HttpThread").setValue((double) (jbossThreadsCount + jettyThreadsCount));
        frameworkThread.findOrCreateExtensionDetail("CatThread").setValue((double) this.countThreadsByPrefix(threads, "Cat-"));
        frameworkThread.findOrCreateExtensionDetail("PigeonThread").setValue(this.countThreadsByPrefix(threads, "Pigeon-", "DPSF-", "Netty-", "Client-ResponseProcessor"));
        frameworkThread.findOrCreateExtensionDetail("ActiveThread").setValue((double) bean.getThreadCount());
        frameworkThread.findOrCreateExtensionDetail("StartedThread").setValue((double) bean.getTotalStartedThreadCount());
        this.m_statusInfo.addExtension(frameworkThread);
    }
}
