package com.github.bannirui.msb.remotecfg;

import com.github.bannirui.msb.common.constant.AppCfg;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import com.github.bannirui.msb.remotecfg.bean.NacosMeta;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 以Spring的生命周期事件为锚点.
 */
public class MySpringApplicationEventListener implements ApplicationListener<SpringApplicationEvent>, PriorityOrdered {

    private NacosMeta nacosMeta;

    public MySpringApplicationEventListener() {
        this.nacosMeta = new NacosMeta();
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        // TODO: 2024/3/29 自定义Exception
        if (!this.check()) {
            throw new RuntimeException("校验条件不过");
        }
        if (event instanceof ApplicationStartingEvent e) {
            this.processApplicationStartingEvent(e);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            this.processApplicationEnvironmentPreparedEvent(e);
        } else if (event instanceof ApplicationStartedEvent e) {
            this.processApplicationStartedEvent(e);
        }
    }

    /**
     * 拉高优先级.
     * 但是远程配置中心的的读写依赖namespace 而namespace被设计为app id
     * 因此优先级要保证在app id之后
     *
     * @see com.github.bannirui.msb.common.listener.MyCfgListener
     */
    @Override
    public int getOrder() {
        // 拉高优先级
        return Integer.MIN_VALUE + 1;
    }

    /**
     * 埋点.
     */
    private boolean check() {
        // TODO: 2024/3/29 不确定前置还是后置
        return true;
    }

    /**
     * ApplicationStartingEvent事件时机锚点.
     * 这个时候还到读取app id的时机
     * 因此这个时候只负责把远程配置中心的url读出来 有异常直接不让spring容器启动
     */
    private void processApplicationStartingEvent(ApplicationStartingEvent e) {
        // TODO: 2024/3/29 把配置中心的url读出来
        this.nacosMeta.setServer("localhost");
    }

    /**
     * 优先级低于{@link com.github.bannirui.msb.common.listener.MyCfgListener}.
     * 此时已经可以拿到缓存好的app id.
     */
    private void processApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent e) {
        ConfigurableEnvironment environment = e.getEnvironment();
        String appId = environment.getProperty(AppCfg.APP_ID_KEY);
        this.nacosMeta.setNamespace(appId);
    }

    /**
     * 应用启动成功 这个时候去拿{@link com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg}注解上属性值.
     */
    private void processApplicationStartedEvent(ApplicationStartedEvent e) {
        // 所有打上注解的Bean
        Map<String, Object> beans = e.getApplicationContext().getBeansWithAnnotation(EnableMyRemoteCfg.class);
        Set<String> dataIdSet = new HashSet<>();
        for (Object bean : beans.values()) {
            // Bean上的注解
            Annotation[] annotations = bean.getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof EnableMyRemoteCfg o) {
                    String[] dataIds = o.dataId();
                    // nacos的data id缓存起来
                    dataIdSet.addAll(Arrays.asList(dataIds));
                }
            }
        }
        this.nacosMeta.setDataIds(new ArrayList<>(dataIdSet));
        // nacos三要素server namespace和data id已经齐了 可以去pull远程配置了
        System.out.println("nacos的url=" + this.nacosMeta.getServer());
        System.out.println("nacos的namespace=" + this.nacosMeta.getNamespace());
        System.out.println("nacos的dataId=" + this.nacosMeta.getDataIds());
    }
}
