package com.github.bannirui.msb.cache.redis.listener;

import com.github.bannirui.msb.cache.redis.DynamicJedisConnectionConfiguration;
import com.github.bannirui.msb.cache.redis.DynamicLettuceConnectionConfiguration;
import com.github.bannirui.msb.cache.redis.connection.DynamicJedisConnectionFactory;
import com.github.bannirui.msb.cache.redis.connection.DynamicLettuceConnectionFactory;
import com.github.bannirui.msb.event.DynamicConfigChangeSpringEvent;
import io.lettuce.core.resource.ClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedisConfigurationRefresherListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, EnvironmentAware, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfigurationRefresherListener.class);
    public static final String ENABLED_DYNAMIC_CONFIG_KEY = "msb.cache.dynamic.redis.enabled";
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Environment env;
    private ApplicationContext context;

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent event) {
        Boolean enabledDynamic = this.env.getProperty(RedisConfigurationRefresherListener.ENABLED_DYNAMIC_CONFIG_KEY, Boolean.class, false);
        if(!Objects.equals(Boolean.TRUE, enabledDynamic)) return;
        logger.info("accept a redis configuration changing event. from {}.", event.getSource());
        Set<String> changedConfigKeys = event.getConfigChange().getChangedConfigKeys();
        if (changedConfigKeys.stream().anyMatch((key) -> key.startsWith("spring.redis"))) {
            RedisProperties redisProperties = Binder.get(this.env).bind("spring.redis", RedisProperties.class).get();
            Object factory = this.context.getBean("dynamicRedisConnectionFactory");
            if (redisProperties != null) {
                if (factory instanceof DynamicLettuceConnectionFactory) {
                    ClientResources clientResources = this.context.getBean(ClientResources.class);
                    DynamicLettuceConnectionConfiguration connectionConfiguration = this.context.getBean(DynamicLettuceConnectionConfiguration.class);
                    LettuceConnectionFactory lettuceConnectionFactory = connectionConfiguration.createLettuceConnectionFactory(connectionConfiguration.getLettuceClientConfiguration(clientResources, redisProperties), redisProperties);
                    LettuceConnectionFactory oldLettuceConnectionFactory = ((DynamicLettuceConnectionFactory)factory).setConnectionFactory(lettuceConnectionFactory);
                    this.releaseLettuceConnectionFactory(oldLettuceConnectionFactory);
                } else {
                    DynamicJedisConnectionConfiguration connectionConfiguration = this.context.getBean(DynamicJedisConnectionConfiguration.class);
                    JedisConnectionFactory newJedisConnectionFactory = connectionConfiguration.createJedisConnectionFactory(redisProperties);
                    JedisConnectionFactory oldJedisConnectionFactory = ((DynamicJedisConnectionFactory)factory).setConnectionFactory(newJedisConnectionFactory);
                    this.releaseJedisConnectionFactory(oldJedisConnectionFactory);
                }
            }
        }
    }

    private void releaseLettuceConnectionFactory(LettuceConnectionFactory factory) {
        LettuceConnectionFactoryReleaseTask task = new LettuceConnectionFactoryReleaseTask(factory, this.executorService);
        this.executorService.schedule(task, 3L, TimeUnit.SECONDS);
    }

    private void releaseJedisConnectionFactory(JedisConnectionFactory factory) {
        JedisConnectionFactoryReleaseTask task = new JedisConnectionFactoryReleaseTask(factory, this.executorService);
        this.executorService.schedule(task, 3L, TimeUnit.SECONDS);
    }

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
