package com.github.bannirui.msb.cache.autoconfig;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.redis.RedisCacheConfigurationCustomizer;
import com.github.bannirui.msb.cache.redis.autoconfig.DynamicRedisAutoConfiguration;
import com.github.bannirui.msb.cache.util.PatchUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@ConditionalOnClass({RedisConnectionFactory.class})
@AutoConfigureAfter({DynamicRedisAutoConfiguration.class, CompositeCacheConfiguration.class})
@ConditionalOnBean({RedisConnectionFactory.class})
@ConditionalOnMissingBean({CacheManager.class})
public class RedisCacheConfig implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);
    private CacheProperties cacheProperties;
    private CompositeCacheProperties compositeCacheProperties;
    private final CacheManagerCustomizers customizers;
    private ApplicationContext applicationContext;
    private RedisCacheConfigurationCustomizer redisCacheConfigCustomizer;

    public RedisCacheConfig(CacheProperties cacheProperties, CompositeCacheProperties compositeCacheProperties, CacheManagerCustomizers customizers, ObjectProvider<RedisCacheConfigurationCustomizer> redisCacheConfigCustomizer) {
        this.cacheProperties = cacheProperties;
        this.compositeCacheProperties = compositeCacheProperties;
        this.customizers = customizers;
        this.redisCacheConfigCustomizer = redisCacheConfigCustomizer.getIfAvailable();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {
        Map<String, CacheProperties.Redis> redis = this.compositeCacheProperties.getRedis();
        Map<String, RedisCacheConfiguration> redisCacheConfigs = new HashMap<>();
        redis.forEach((cacheName, cacheConfig) -> {
            RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(cacheConfig.getTimeToLive());
            if (!cacheConfig.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }
            if (!cacheConfig.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }
            config = PatchUtil.invokeRedisCacheConfigCustomizer(cacheName, config, this.redisCacheConfigCustomizer);
            redisCacheConfigs.put(cacheName, config);
        });
        PatchUtil.redisPatch(redisCacheConfigs, this.applicationContext, this.compositeCacheProperties, this.redisCacheConfigCustomizer);
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(this.determineRedisConfiguration(resourceLoader.getClassLoader())).withInitialCacheConfigurations(redisCacheConfigs);
        RedisCacheManager cacheManager = builder.build();
        RedisCacheManager customize = this.customizers.customize(cacheManager);
        logger.info("RedisCacheManager {} has created.", customize);
        return customize;
    }

    private RedisCacheConfiguration determineRedisConfiguration(ClassLoader classLoader) {
        CacheProperties.Redis redisProperties = this.cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(classLoader)));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        config = PatchUtil.invokeRedisCacheConfigCustomizer((String)null, config, this.redisCacheConfigCustomizer);
        return config;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
