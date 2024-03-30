package com.github.bannirui.msb.remotecfg.util;

import java.util.Collections;
import java.util.Map;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * 文本配置转换.
 */
public class ConfigPropertyUtil {

    /**
     * 解析配置内容.
     *
     * @param content \n换行 =分割行
     */
    public static CompositePropertySource parse(String content, String id) {
        if (content == null || content.isBlank() || id == null || id.isBlank()) {
            return null;
        }
        String[] lines = content.split("\n");
        int sz = 0;
        if ((sz = lines.length) == 0) {
            return null;
        }
        CompositePropertySource composite = new CompositePropertySource("nacos config " + id);
        for (int i = 0; i < sz; i++) {
            String line = lines[i];
            String[] kv = line.split("=");
            if (kv.length != 2) {
                continue;
            }
            String k = kv[0].trim();
            String v = kv[1].trim();
            PropertySource<Map<String, Object>> ps = new MapPropertySource("ps" + i, Collections.singletonMap(k, v));
            composite.addPropertySource(ps);
        }
        return composite;
    }
}
