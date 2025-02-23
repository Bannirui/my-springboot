package com.github.bannirui.msb.cache.autoconfig;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.caffeine.CaffeineCacheEnhanceManager;
import com.github.bannirui.msb.cache.composite.CompositeCacheManager;
import com.github.bannirui.msb.cache.redis.RedisCacheConfigurationCustomizer;
import com.github.bannirui.msb.cache.redis.autoconfig.DynamicRedisAutoConfiguration;
import com.github.bannirui.msb.cache.util.PatchUtil;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
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
@ConditionalOnClass({RedisConnectionFactory.class, Caffeine.class, CaffeineCacheEnhanceManager.class})
@ConditionalOnBean({RedisConnectionFactory.class})
@ConditionalOnMissingBean({CacheManager.class})
@AutoConfigureAfter({DynamicRedisAutoConfiguration.class})
public class CompositeCacheConfiguration implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CompositeCacheConfiguration.class);
    private final CacheProperties cacheProperties;
    private final CompositeCacheProperties compositeCacheProperties;
    private final Caffeine<Object, Object> caffeine;
    private final CaffeineSpec caffeineSpec;
    private final CacheLoader<Object, Object> cacheLoader;
    private final CacheManagerCustomizers customizers;
    private ApplicationContext applicationContext;
    private RedisCacheConfigurationCustomizer redisCacheConfigCustomizer;

    public CompositeCacheConfiguration(CacheProperties cacheProperties, CompositeCacheProperties compositeCacheProperties, CacheManagerCustomizers customizers, ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec, ObjectProvider<RedisCacheConfigurationCustomizer> redisCacheConfigCustomizer, ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        this.cacheProperties = cacheProperties;
        this.compositeCacheProperties = compositeCacheProperties;
        this.customizers = customizers;
        this.caffeine = caffeine.getIfAvailable();
        this.caffeineSpec = caffeineSpec.getIfAvailable();
        this.cacheLoader = cacheLoader.getIfAvailable();
        this.redisCacheConfigCustomizer = redisCacheConfigCustomizer.getIfAvailable();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {
        CacheManager local = this.localCacheManager(this.compositeCacheProperties);
        CacheManager remote = this.remoteCacheManager(redisConnectionFactory, resourceLoader);
        CompositeCacheManager cacheManager = new CompositeCacheManager(local, remote, this.compositeCacheProperties, this.cacheProperties);
        logger.info("CompositeCacheManager {} has created. local cache: {}, remote cache: {}", cacheManager, local, remote);
        return cacheManager;
    }

    private CacheManager localCacheManager(CompositeCacheProperties properties) {
        Map<String, Caffeine> caffeineMap = new HashMap<>();
        Map<String, CacheProperties.Caffeine> caffeineProperties = properties.getCaffeine();
        if (Objects.nonNull(caffeineProperties)) {
            caffeineProperties.entrySet().stream().forEach((entry) -> {
                caffeineMap.put(entry.getKey(), Caffeine.from((entry.getValue()).getSpec()));
            });
        }
        PatchUtil.caffeinePatch(caffeineMap, this.applicationContext, properties);
        CaffeineCacheEnhanceManager cacheManager = this.createCaffeineManager(caffeineMap);
        if (!properties.isAllowNullValues()) {
            cacheManager.setAllowNullValues(false);
        }
        if (this.cacheLoader != null) {
            cacheManager.setCacheLoader(this.cacheLoader);
        }
        cacheManager.afterPropertiesSet();
        return this.customizers.customize(cacheManager);
    }

    private CaffeineCacheEnhanceManager createCaffeineManager(Map<String, Caffeine> caffeineMap) {
        CaffeineCacheEnhanceManager cacheManager = new CaffeineCacheEnhanceManager(caffeineMap);
        if (StringUtils.isEmpty(this.cacheProperties.getCaffeine().getSpec())) {
            logger.warn("caffeine spec in CacheProperties is not configured.");
        } else if (!StringUtils.isEmpty(this.cacheProperties.getCaffeine().getSpec())) {
            cacheManager.setCaffeineSpecification(this.cacheProperties.getCaffeine().getSpec());
        } else if (this.caffeineSpec != null) {
            cacheManager.setCaffeineSpec(this.caffeineSpec);
        } else if (this.caffeine != null) {
            cacheManager.setCaffeine(this.caffeine);
        }
        return cacheManager;
    }

    private CacheManager remoteCacheManager(RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {
        Map<String, CacheProperties.Redis> redis = this.compositeCacheProperties.getRedis();
        Map<String, RedisCacheConfiguration> redisCacheConfigs = new HashMap<>();
        redis.forEach((cacheName, cacheConfig) -> {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Objects.nonNull(cacheConfig.getTimeToLive()) ? cacheConfig.getTimeToLive() : this.cacheProperties.getRedis().getTimeToLive());
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
        cacheManager.afterPropertiesSet();
        return this.customizers.customize(cacheManager);
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
        config = PatchUtil.invokeRedisCacheConfigCustomizer(null, config, this.redisCacheConfigCustomizer);
        return config;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
