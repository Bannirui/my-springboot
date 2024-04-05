package com.github.bannirui.msb.remotecfg.executor;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.github.bannirui.msb.common.exception.UnsupportedException;
import com.github.bannirui.msb.remotecfg.MySpringApplicationEventListener;
import com.github.bannirui.msb.remotecfg.spring.SpringValueAnnotationProcessor;
import com.github.bannirui.msb.remotecfg.spring.bean.BeanWithValueAnnotation;
import com.github.bannirui.msb.remotecfg.spring.bean.FieldValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.MethodValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotatedObj;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotationTarget;
import com.github.bannirui.msb.remotecfg.util.ConfigPropertyUtil;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 监听器.
 * 监听远程配置中心的回调.
 * 启动时机放在Spring应用已经准备好.
 * 因为要读到远程配置中心的信息 所以放到{@link MySpringApplicationEventListener}中去.
 */
public class HotReplaceListener extends Thread {

    private MySpringApplicationEventListener listener;

    /**
     * 跟{@link MySpringApplicationEventListener}耦合的原因是为了读取它的成员(nacos的配置信息).
     */
    public HotReplaceListener(MySpringApplicationEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        String nacosUrl = this.listener.getNacosMeta().getServer();
        List<String> dataIds = this.listener.getNacosMeta().getDataIds();
        String group = "DEFAULT_GROUP";
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosUrl);
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (configService == null) {
            return;
        }
        for (String dataId : dataIds) {
            // 轮询注册监听器
            try {
                configService.addListener(dataId, group, new Listener() {

                    /**
                     * nacos配置中心发布更新 回调给监听器的竟然是整个data id的所有配置内容
                     * @param content 配置内容
                     */
                    @Override
                    public void receiveConfigInfo(String content) {
                        if (content == null || content.isBlank()) {
                            return;
                        }
                        // 最新解析出来的
                        Map<String, String> cur = ConfigPropertyUtil.parse(content);
                        // 缓存好的
                        Map<String, ValueAnnotatedObj> cached = SpringValueAnnotationProcessor.getPlaceholderPair();
                        if (cur == null || cur.isEmpty()) {
                            return;
                        }
                        cur.forEach((k, v) -> {
                            ValueAnnotatedObj valueAnnotatedObj = cached.get(k);
                            String pre = valueAnnotatedObj.getLastVal();
                            if (!Objects.equals(v, pre)) {
                                // 配置发生了变化 反射所有作用的实例
                                for (BeanWithValueAnnotation o : valueAnnotatedObj.getList()) {
                                    WeakReference<Object> beanRef = o.getBean();
                                    ValueAnnotationAttr attr = o.getAnnotationAttr();
                                    ValueAnnotationTarget.TargetType targetType = attr.getTargetType();
                                    if (targetType == ValueAnnotationTarget.TargetType.FIELD) {
                                        // 注解最用在成员上
                                        FieldValueAnnotationAttr realAttr = (FieldValueAnnotationAttr) attr;
                                        Field field = realAttr.getField();
                                        Class<?> propertyType = realAttr.getPropertyType();
                                        field.setAccessible(true);
                                        try {
                                            if (propertyType.equals(String.class)) {
                                                field.set(beanRef.get(), v);
                                            } else if (propertyType.equals(Integer.class)) {
                                                field.set(beanRef.get(), Integer.valueOf(v));
                                            } else if (propertyType.equals(Long.class)) {
                                                field.set(beanRef.get(), Long.valueOf(v));
                                            } else if (propertyType.equals(Short.class)) {
                                                field.set(beanRef.get(), Short.valueOf(v));
                                            } else if (propertyType.equals(List.class)) {
                                                // TODO: 2024/4/5 java是运行时容器范型擦除了 怎么确定元素的类型呢
                                                throw new UnsupportedException("@Value的远程配置热更新不支持" + propertyType + "类型");
                                            } else {
                                                throw new UnsupportedException("@Value的远程配置热更新不支持" + propertyType + "类型");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else if (targetType == ValueAnnotationTarget.TargetType.METHOD) {
                                        // 注解作用在方法上
                                        MethodValueAnnotationAttr realAttr = (MethodValueAnnotationAttr) attr;
                                        Method method = realAttr.getMethod();
                                        Class<?> propertyType = realAttr.getPropertyType();
                                        try {
                                            if (propertyType.equals(String.class)) {
                                                method.invoke(beanRef.get(), v);
                                            } else if (propertyType.equals(Integer.class)) {
                                                method.invoke(beanRef.get(), Integer.valueOf(v));
                                            } else if (propertyType.equals(Long.class)) {
                                                method.invoke(beanRef.get(), Long.valueOf(v));
                                            } else if (propertyType.equals(Short.class)) {
                                                method.invoke(beanRef.get(), Short.valueOf(v));
                                            } else if (propertyType.equals(List.class)) {
                                                throw new UnsupportedException("@Value的远程配置热更新不支持" + propertyType + "类型");
                                            } else {
                                                throw new UnsupportedException("@Value的远程配置热更新不支持" + propertyType + "类型");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
