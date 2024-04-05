package com.github.bannirui.msb.remotecfg.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * 文本配置转换.
 */
public class ConfigPropertyUtil {

    /**
     * 解析nacos配置.
     *
     * @param content nacos配置中心的内容. \n换行 =分割行
     */
    public static Map<String, String> parse(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String[] lines = content.split("\n");
        int sz = 0;
        if ((sz = lines.length) == 0) {
            return null;
        }
        Map<String, String> ans = new HashMap<>();
        for (int i = 0; i < sz; i++) {
            String line = lines[i];
            String[] kv = line.split("=");
            if (kv.length != 2) {
                continue;
            }
            // 配置项
            String k = kv[0].trim();
            // 配置值
            String v = kv[1].trim();
            ans.put(k, v);
        }
        return ans;
    }

    public static CompositePropertySource parse(Map<String, String> source, String nacosDataId) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        CompositePropertySource composite = new CompositePropertySource("NacosConfig " + nacosDataId);
        int i = 0;
        for (Map.Entry<String, String> entry : source.entrySet()) {
            PropertySource<Map<String, Object>> ps =
                new MapPropertySource("NacosItem" + i++, Collections.singletonMap(entry.getKey(), entry.getValue()));
            composite.addPropertySource(ps);
        }
        return composite;
    }

    /**
     * 解析配置内容.
     *
     * @param nacosDataId nacos的data id
     */
    public static CompositePropertySource parse(String content, String nacosDataId) {
        Map<String, String> map = null;
        if (content == null || content.isBlank() || (map = parse(content)) == null || map.isEmpty()) {
            return null;
        }
        return parse(map, nacosDataId);
    }
}
