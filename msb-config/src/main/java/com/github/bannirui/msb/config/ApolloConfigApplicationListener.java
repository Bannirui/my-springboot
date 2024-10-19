package com.github.bannirui.msb.config;


import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.config.spring.ConfigPropertySourceFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 整合Apollo的入口
 * <ul>
 *     <li>容器启动的时候缓存业务应用需要关注的namespace</li>
 *     <li>Environment就绪的时候获取上一步缓存的所有namespace对应的配置加载到内存中</li>
 * </ul>
 */
public class ApolloConfigApplicationListener implements ApplicationListener<SpringApplicationEvent>, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(ApolloConfigApplicationListener.class);
    /**
     * key是{@link EnableMsbConfig}指定的远程配置的优先级.
     * value是远程配置的namespace
     * 容器Environment准备好后找到容器中所有打了这个注解的类取到远程配置的namespace
     */
    private static final Multimap<Integer, String> NAMESPACE_NAMES = HashMultimap.create();
    private static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";
    private ConfigPropertySourceFactory configPropertySourceFactory =
        (ConfigPropertySourceFactory) SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent e) {
            this.applicationStartingEvent(e);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            this.envPreparedEvent(e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 容器启动 将注解中必要的apollo元信息缓存起来后面使用.
     */
    private void applicationStartingEvent(ApplicationStartingEvent event) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Properties props = new Properties();
        try {
            Resource[] resources = resolver.getResources("classpath*:/META-INF/msb/apollo-env.properties");
            if (resources != null && resources.length > 0) {
                for (Resource resource : resources) {
                    props.load(resource.getInputStream());
                }
            }
        } catch (Exception e) {
            logger.warn("msb apollo-env.properties 加载失败, err=", e);
        }
        String dev, fat, uat, sit, prod;
        String netEnv = EnvironmentMgr.getNetEnv();
        if (Objects.isNull(netEnv)) {
            dev = System.getProperty("dev_meta", props.getProperty("dev.meta"));
            fat = System.getProperty("fat_meta", props.getProperty("fat.meta"));
            uat = System.getProperty("uat_meta", props.getProperty("uat.meta"));
            sit = System.getProperty("sit_meta", props.getProperty("sit.meta"));
            prod = System.getProperty("prod_meta", props.getProperty("prod.meta"));
        } else {
            dev = System.getProperty(netEnv + "_dev_meta", props.getProperty(netEnv + "dev.meta"));
            fat = System.getProperty(netEnv + "_fat_meta", props.getProperty(netEnv + "fat.meta"));
            uat = System.getProperty(netEnv + "_uat_meta", props.getProperty(netEnv + "uat.meta"));
            sit = System.getProperty(netEnv + "_sit_meta", props.getProperty(netEnv + "sit.meta"));
            prod = System.getProperty(netEnv + "_prod_meta", props.getProperty(netEnv + "prod.meta"));
        }
        // TODO: 2024/10/19
    }

    /**
     * Spring Environment就绪.
     * 将配置中心apollo的配置内容同步到缓存中.
     */
    private void envPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        if (event.getEnvironment().getPropertySources().contains(APOLLO_PROPERTY_SOURCE_NAME)) {
            return;
        }
        Set<Object> sources = event.getSpringApplication().getAllSources();
        this.initNamespaces(sources);
        CompositePropertySource composite = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);
        ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
        for (Integer order : orders) {
            for (String namespace : NAMESPACE_NAMES.get(order)) {
                Config config = ConfigService.getConfig(namespace);
                ConfigPropertySource configPropertySource = this.configPropertySourceFactory.getConfigPropertySource(namespace, config);
                composite.addFirstPropertySource(configPropertySource);
            }
        }
        event.getEnvironment().getPropertySources().addLast(composite);
    }

    private void initNamespaces(Set<Object> sources) {
        sources.forEach(x ->
        {
            Class clz = (Class) x;
            if (clz.isAnnotationPresent(EnableMsbConfig.class)) {
                EnableMsbConfig annotation = (EnableMsbConfig) clz.getDeclaredAnnotation(EnableMsbConfig.class);
                NAMESPACE_NAMES.putAll(annotation.order(), Lists.newArrayList(annotation.value()));
            }
        });
    }
}
