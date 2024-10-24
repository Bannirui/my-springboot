package com.github.bannirui.msb.common.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigChangeListenerContainer {
    private static final Map<String, List<ConfigChangeListenerMetaData>> container = new HashMap<>();
    private static final Set<ConfigChangeListenerMetaData> all = new HashSet<>();

    public ConfigChangeListenerContainer() {
    }

    public static void addConfigChangeListenerMetaData(String key, ConfigChangeListenerMetaData metaData) {
        if (container.containsKey(key)) {
            container.get(key).add(metaData);
        } else {
            List<ConfigChangeListenerMetaData> metaDatas = new ArrayList<>();
            metaDatas.add(metaData);
            container.put(key, metaDatas);
        }

    }

    public static Map<String, List<ConfigChangeListenerMetaData>> getAllMappings() {
        return container;
    }

    public static Set<ConfigChangeListenerMetaData> getCheckedMetaData(Set<String> changedKeys) {
        if (changedKeys != null && !changedKeys.isEmpty()) {
            Set<ConfigChangeListenerMetaData> result = new HashSet<>();
            container.keySet().forEach((key) -> {
                Pattern p = Pattern.compile(key);
                for (String changedKey : changedKeys) {
                    if (changedKey.equals(key)) {
                        result.addAll(container.get(key));
                    } else {
                        Matcher m = p.matcher(changedKey);
                        if (m.find() && m.groupCount() == 1) {
                            result.addAll(container.get(key));
                        }
                    }
                }
            });
            result.addAll(all);
            return result;
        } else {
            return null;
        }
    }

    public static void addListenAllKeysListenerMetaData(ConfigChangeListenerMetaData metaData) {
        all.add(metaData);
    }
}
