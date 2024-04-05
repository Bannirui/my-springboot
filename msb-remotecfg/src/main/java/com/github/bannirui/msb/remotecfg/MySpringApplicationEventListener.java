package com.github.bannirui.msb.remotecfg;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import com.github.bannirui.msb.common.constant.EnvType;
import com.github.bannirui.msb.common.exception.InvalidException;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import com.github.bannirui.msb.remotecfg.bean.NacosMeta;
import com.github.bannirui.msb.remotecfg.executor.HotReplaceListener;
import com.github.bannirui.msb.remotecfg.executor.HotReplaceMgr;
import com.github.bannirui.msb.remotecfg.spring.SpringValueAnnotationProcessor;
import com.github.bannirui.msb.remotecfg.util.ConfigPropertyUtil;
import com.github.bannirui.msb.remotecfg.util.NacosClientUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
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

    // 远程的配置中心数据
    private static final Map<String, String> REMOTE_DATA = new HashMap<>();

    public MySpringApplicationEventListener() {
        this.nacosMeta = new NacosMeta();
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (!this.check()) {
            throw new InvalidException("校验条件不过 启动参数没有指定环境");
        }
        if (event instanceof ApplicationStartingEvent) {
            // 开始启动中
            this.loadNacosUrl();
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            // 环境已经准备好
        } else if (event instanceof ApplicationContextInitializedEvent) {
            // 上下文已经实例化好
        } else if (event instanceof ApplicationPreparedEvent e) {
            // Spring上下文已经准备好
            this.loadNacosData(e);
        } else if (event instanceof ApplicationStartedEvent) {
            // 应用启动成功
        } else if (event instanceof ApplicationReadyEvent) {
            // Spring已经准备好
            this.registerNacosListener();
            this.boostNocosCandidateMgr();
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

    public NacosMeta getNacosMeta() {
        return nacosMeta;
    }

    public static Map<String, String> getRemoteData() {
        return REMOTE_DATA;
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
    private void loadNacosUrl() {
        Properties props = new Properties();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:remote_url.properties");
            int sz = 0;
            if ((sz = resources.length) > 0) {
                for (int i = 0; i < sz; i++) {
                    Resource resource = resources[i];
                    props.load(resource.getInputStream());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String env = System.getProperty(EnvType.KEY);
        String remoteCfgServerAddr = props.getProperty(env);
        this.nacosMeta.setServer(remoteCfgServerAddr);
    }

    /**
     * 趁着Bean还没实例化好.
     * 此时可以拿到部分Bean的BeanDefinition信息.
     * 这个时候去拿{@link com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg}注解上属性值.
     * 把远程配置中心的dataId属性读出来.
     * 然后将配置中心数据读到内存.
     */
    private void loadNacosData(ApplicationPreparedEvent e) {
        // 加载Nacos的DataId属性
        this.loadNacosDataId(e);
        // nacos的server和dataId已经齐了 可以去pull远程配置了
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
            // 读出来的配置kv键值对一方面存储到Environment中 另一方面填充缓存好的原始值
            Map<String, String> map = ConfigPropertyUtil.parse(content);
            CompositePropertySource source = ConfigPropertyUtil.parse(map, dataId);
            if (source == null) {
                continue;
            }
            // 配置项放到Spring中
            e.getApplicationContext().getEnvironment().getPropertySources().addLast(source);
            // 填充缓存信息
            this.doCache(map);
        }
    }

    /**
     * 配置中心的数据缓存起来.
     * 后面{@link SpringValueAnnotationProcessor}在Bean实例化好初始化前会把{@link org.springframework.beans.factory.annotation.Value}的注解信息缓存起来.
     * 届时把这一部分也一并缓存起来.
     */
    private void doCache(final Map<String, String> nacosData) {
        if (nacosData.isEmpty()) {
            return;
        }
        REMOTE_DATA.putAll(nacosData);
    }

    /**
     * 从{@link EnableMyRemoteCfg}注解中拿到dataId属性缓存起来.
     */
    private void loadNacosDataId(ApplicationPreparedEvent e) {
        String[] names = e.getApplicationContext().getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition beanDefinition = e.getApplicationContext().getBeanFactory().getBeanDefinition(name);
            String beanClassName = beanDefinition.getBeanClassName();
            Class<?> clazz = null;
            try {
                clazz = Class.forName(beanClassName);
            } catch (Exception ex) {
                throw new InvalidException("找不到类" + beanClassName);
            }
            if (clazz.isAnnotationPresent(EnableMyFramework.class) && this.enableRemoteCfgAnnotationCheck4MainClass(clazz)) {
                EnableMyRemoteCfg annotation = clazz.getAnnotation(EnableMyRemoteCfg.class);
                String[] dataIds = annotation.dataId();
                // nacos的data id缓存起来
                Set<String> dataIdSet = new HashSet<>(Arrays.asList(dataIds));
                this.nacosMeta.setDataIds(new ArrayList<>(dataIdSet));
                // 注解只能打在启动类上约束了只有一个BeanClass会被打上注解.
                return;
            }
        }
    }

    /**
     * 向nacos注册监听器.
     */
    private void registerNacosListener() {
        new HotReplaceListener(this).start();
    }

    /**
     * 启动类的检查.
     * 确保启动类上有注解{@link EnableMyRemoteCfg}
     */
    private boolean enableRemoteCfgAnnotationCheck4MainClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(EnableMyRemoteCfg.class);
    }

    /**
     * 启动管理器 定期清理过期的数据.
     */
    private void boostNocosCandidateMgr() {
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "HotReplace-Thread"))
            .scheduleAtFixedRate(new HotReplaceMgr(), 5L, 5L, TimeUnit.SECONDS);
    }
}
