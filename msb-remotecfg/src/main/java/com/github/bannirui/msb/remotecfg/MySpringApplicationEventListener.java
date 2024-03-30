package com.github.bannirui.msb.remotecfg;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import com.github.bannirui.msb.common.constant.EnvType;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import com.github.bannirui.msb.remotecfg.bean.NacosMeta;
import com.github.bannirui.msb.remotecfg.util.ConfigPropertyUtil;
import com.github.bannirui.msb.remotecfg.util.NacosClientUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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
            throw new RuntimeException("校验条件不过 启动参数没有指定环境");
        }
        if (event instanceof ApplicationStartingEvent e) {
            this.processApplicationStartingEvent(e);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            this.processApplicationEnvironmentPreparedEvent(e);
        } else if (event instanceof ApplicationPreparedEvent e) {
            this.processApplicationPreparedEvent(e);
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
     * <ul>启动参数指定环境-Denv=xxx</ul>
     */
    private boolean check() {
        String env = System.getProperty(EnvType.KEY);
        if (env == null || env.isBlank()) {
            return false;
        }
        return true;
    }

    /**
     * ApplicationStartingEvent事件时机锚点.
     * 因此这个时候只负责把远程配置中心的url读出来 有异常直接不让spring容器启动.
     */
    private void processApplicationStartingEvent(ApplicationStartingEvent e) {
        Properties props = new Properties();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:remote_url.properties");
            int sz = 0;
            if (resources != null && (sz = resources.length) > 0) {
                for (int i = 0; i < sz; i++) {
                    Resource resource = resources[i];
                    props.load(resource.getInputStream());
                }
            }
        } catch (Exception var10) {
        }
        String env = System.getProperty(EnvType.KEY);
        String remoteCfgServerAddr = props.getProperty(env);
        this.nacosMeta.setServer(remoteCfgServerAddr);
    }

    /**
     * 优先级低于{@link com.github.bannirui.msb.common.listener.MyCfgListener}.
     * 此时已经可以拿到缓存好的app id.
     * 以后可能需要用app id做别的事情.
     */
    private void processApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent e) {
    }

    /**
     * 趁着Bean还没实例化好.
     * 此时可以拿到部分Bean的BeanDefinition信息.
     * 这个时候去拿{@link com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg}注解上属性值.
     * 把远程配置中心的信息获取出来.
     */
    private void processApplicationPreparedEvent(ApplicationPreparedEvent e) {
        String[] names = e.getApplicationContext().getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition beanDefinition = e.getApplicationContext().getBeanFactory().getBeanDefinition(name);
            String beanClassName = beanDefinition.getBeanClassName();
            Class<?> clazz = null;
            try {
                clazz = Class.forName(beanClassName);
            } catch (Exception ex) {
                throw new RuntimeException("找不到类");
            }
            if (clazz.isAnnotationPresent(EnableMyFramework.class) && this.enableRemoteCfgAnnotationCheck4MainClass(clazz)) {
                EnableMyRemoteCfg annotation = clazz.getAnnotation(EnableMyRemoteCfg.class);
                String[] dataIds = annotation.dataId();
                // nacos的data id缓存起来
                Set<String> dataIdSet = new HashSet<>(Arrays.asList(dataIds));
                this.nacosMeta.setDataIds(new ArrayList<>(dataIdSet));
            }
        }
        // nacos三要素server namespace和data id已经齐了 可以去pull远程配置了
        List<String> dataIds = this.nacosMeta.getDataIds();
        if (dataIds.isEmpty()) {
            return;
        }
        String server = this.nacosMeta.getServer();

        for (String dataId : dataIds) {
            String content = NacosClientUtil.getConfig(server, dataId);
            if (content == null || content.isBlank()) {
                continue;
            }
            CompositePropertySource source = ConfigPropertyUtil.parse(content, dataId);
            // 配置项放到Spring中
            e.getApplicationContext().getEnvironment().getPropertySources().addLast(source);
        }
    }

    /**
     * 启动类的检查.
     * 确保启动类上有注解{@link EnableMyRemoteCfg}
     */
    private boolean enableRemoteCfgAnnotationCheck4MainClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(EnableMyRemoteCfg.class);
    }
}
