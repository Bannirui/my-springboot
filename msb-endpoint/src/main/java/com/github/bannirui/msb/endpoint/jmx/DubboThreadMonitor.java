package com.github.bannirui.msb.endpoint.jmx;

import com.alibaba.dubbo.common.Constants;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;

public class DubboThreadMonitor implements MonitorForLogger, MonitorForCat {

    @Override
    public String getId() {
        return "Dubbo thread";
    }

    @Override
    public String getDescription() {
        return "Dubbo thread pool info";
    }

    @Override
    public Map<String, String> getProperties() {
        HashMap<String, String> monitorMap = new HashMap<>();
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);
        for (Map.Entry<String, Object> entry : executors.entrySet()) {
            String port = entry.getKey();
            ExecutorService executor = (ExecutorService)entry.getValue();
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tp = (ThreadPoolExecutor)executor;
                monitorMap.put("dubbo." + port + ".max", String.valueOf(tp.getMaximumPoolSize()));
                monitorMap.put("dubbo." + port + ".core", String.valueOf(tp.getCorePoolSize()));
                monitorMap.put("dubbo." + port + ".largest", String.valueOf(tp.getLargestPoolSize()));
                monitorMap.put("dubbo." + port + ".active", String.valueOf(tp.getActiveCount()));
                monitorMap.put("dubbo." + port + ".task", String.valueOf(tp.getTaskCount()));
            }
        }
        return monitorMap;
    }

    @Override
    public Map<String, String> monitor() {
        return this.getProperties();
    }
}
