package com.github.bannirui.msb.remotecfg.executor;

import com.github.bannirui.msb.remotecfg.spring.SpringValueAnnotationProcessor;
import com.github.bannirui.msb.remotecfg.spring.bean.BeanWithValueAnnotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理器.
 * 管理什么呢
 * {@link com.github.bannirui.msb.remotecfg.spring.bean.BeanWithValueAnnotation}bean成员为null时就进行列表清理.
 */
public class HotReplaceMgr extends Thread {

    public HotReplaceMgr() {
    }

    @Override
    public void run() {
        // 缓存好的@Value注解的Bean对象
        Map<String, List<BeanWithValueAnnotation>> map = SpringValueAnnotationProcessor.getValueAnnotationCache();
        Iterator<Map.Entry<String, List<BeanWithValueAnnotation>>> mapIt = map.entrySet().iterator();
        while (mapIt.hasNext()) {
            Map.Entry<String, List<BeanWithValueAnnotation>> kv = mapIt.next();
            List<BeanWithValueAnnotation> ls = kv.getValue();
            if (ls.isEmpty()) {
                mapIt.remove();
                continue;
            }
            // 弱引用对象被回收了就清理掉
            ls.removeIf(Objects::isNull);
        }
    }
}
