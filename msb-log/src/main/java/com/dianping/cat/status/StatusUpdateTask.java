package com.dianping.cat.status;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

public class StatusUpdateTask implements Threads.Task, Initializable {
    @Inject
    private MessageStatistics m_statistics;
    @Inject
    private ClientConfigManager m_manager;
    private boolean m_active = true;
    private String m_ipAddress;
    private long m_interval = 60_000L;
    private String m_jars;

    private void buildClasspath() {
        ClassLoader loader = StatusUpdateTask.class.getClassLoader();
        StringBuilder sb = new StringBuilder();
        this.buildClasspath(loader, sb);
        if (sb.length() > 0) {
            this.m_jars = sb.substring(0, sb.length() - 1);
        }
    }

    private void buildClasspath(ClassLoader loader, StringBuilder sb) {
        if (loader instanceof URLClassLoader urlClassLoader) {
            URL[] urLs = urlClassLoader.getURLs();
            for (URL url : urLs) {
                String jar = this.parseJar(url.toExternalForm());
                if (jar != null) {
                    sb.append(jar).append(',');
                }
            }
            ClassLoader parent = loader.getParent();
            this.buildClasspath(parent, sb);
        }
    }

    private void buildExtensionData(StatusInfo status) {
        StatusExtensionRegister res = StatusExtensionRegister.getInstance();
        List<StatusExtension> extensions = res.getStatusExtension();
        extensions.forEach(extension -> {
            String id = extension.getId();
            String des = extension.getDescription();
            Map<String, String> propertis = extension.getProperties();
            Extension item = status.findOrCreateExtension(id).setDescription(des);
            propertis.forEach((k, v) -> {
                try {
                    double value = Double.parseDouble(v);
                    item.findOrCreateExtensionDetail(k).setValue(value);
                } catch (Exception e) {
                    Cat.logError("StatusExtension can only be double type", e);
                }
            });
        });
    }

    @Override
    public String getName() {
        return "StatusUpdateTask";
    }

    @Override
    public void initialize() throws InitializationException {
        this.m_ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
    }

    private String parseJar(String path) {
        if (path.endsWith(".jar")) {
            int index = path.lastIndexOf(47);
            if (index > -1) {
                return path.substring(index + 1);
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10_000L);
        } catch (InterruptedException var20) {
            return;
        }
        while (true) {
            Calendar cal = Calendar.getInstance();
            int second = cal.get(13);
            if (second >= 2 && second <= 58) {
                try {
                    this.buildClasspath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MessageProducer cat = Cat.getProducer();
                Transaction reboot = cat.newTransaction("System", "Reboot");
                reboot.setStatus("0");
                cat.logEvent("Reboot", NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), "0", null);
                reboot.complete();
                while (this.m_active) {
                    long start = MilliSecondTimer.currentTimeMillis();
                    if (this.m_manager.isCatEnabled()) {
                        Transaction t = cat.newTransaction("System", "Status");
                        Heartbeat h = cat.newHeartbeat("Heartbeat", this.m_ipAddress);
                        StatusInfo status = new StatusInfo();
                        t.addData("dumpLocked", this.m_manager.isDumpLocked());
                        try {
                            StatusInfoCollector statusInfoCollector = new StatusInfoCollector(this.m_statistics, this.m_jars);
                            status.accept(statusInfoCollector.setDumpLocked(this.m_manager.isDumpLocked()));
                            this.buildExtensionData(status);
                            h.addData(status.toString());
                            h.setStatus("0");
                        } catch (Throwable e) {
                            h.setStatus(e);
                            cat.logError(e);
                        } finally {
                            h.complete();
                        }
                        t.setStatus("0");
                        t.complete();
                    }
                    long elapsed = MilliSecondTimer.currentTimeMillis() - start;
                    if (elapsed < this.m_interval) {
                        try {
                            Thread.sleep(this.m_interval - elapsed);
                        } catch (InterruptedException ignored) {
                            break;
                        }
                    }
                }
                return;
            }
            try {
                Thread.sleep(1_000L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void setInterval(long interval) {
        this.m_interval = interval;
    }

    @Override
    public void shutdown() {
        this.m_active = false;
    }
}
