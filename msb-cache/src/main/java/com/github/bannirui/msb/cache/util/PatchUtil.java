package com.github.bannirui.msb.cache.util;

import com.github.bannirui.msb.cache.CacheType;
import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.annotation.CacheConfigOptions;
import com.github.bannirui.msb.cache.annotation.CacheConfigOptionsContainer;
import com.github.bannirui.msb.cache.redis.RedisCacheConfigurationCustomizer;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

public class PatchUtil {
    static final int UNSET_INT = -1;

    public static void redisPatch(Map<String, RedisCacheConfiguration> redisCacheConfigs, ApplicationContext applicationContext, CompositeCacheProperties properties, RedisCacheConfigurationCustomizer customizer) {
        Map<String, Object> customizedCacheAnnotationBeans = new HashMap<>();
        customizedCacheAnnotationBeans.putAll(applicationContext.getBeansWithAnnotation(CacheConfigOptions.class));
        customizedCacheAnnotationBeans.putAll(applicationContext.getBeansWithAnnotation(CacheConfigOptionsContainer.class));
        if (!customizedCacheAnnotationBeans.isEmpty()) {
            customizedCacheAnnotationBeans.forEach((beanName, bean) -> {
                Class<?> targetClass = AopUtils.getTargetClass(bean);
                CacheConfigOptions cacheConfigOptions = AnnotationUtils.findAnnotation(targetClass, CacheConfigOptions.class);
                if (cacheConfigOptions != null) {
                    String[] cacheNames = cacheConfigOptions.cacheNames();
                    long expired = cacheConfigOptions.expired();
                    determineRedisConfiguration(redisCacheConfigs, cacheConfigOptions, expired, cacheNames, properties, customizer);
                }
                CacheConfigOptionsContainer cacheConfigOptionsContainer = AnnotationUtils.findAnnotation(targetClass, CacheConfigOptionsContainer.class);
                if (cacheConfigOptionsContainer != null) {
                    CacheConfigOptions[] options = cacheConfigOptionsContainer.value();
                    for (CacheConfigOptions option : options) {
                        String[] names = option.cacheNames();
                        determineRedisConfiguration(redisCacheConfigs, option, option.expired(), names, properties, customizer);
                    }
                }
            });
        }
    }

    private static void determineRedisConfiguration(Map<String, RedisCacheConfiguration> redisCacheConfigs, CacheConfigOptions cacheConfigOptions, long expired, String[] cacheNames, CompositeCacheProperties properties, RedisCacheConfigurationCustomizer customizer) {
        Map<String, CacheType> cacheType = properties.getType();
        if (Objects.isNull(cacheType)) {
            cacheType = new HashMap<>();
        }
        for (String name : cacheNames) {
            if (!redisCacheConfigs.containsKey(name)) {
                RedisCacheConfiguration customizedConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(expired));
                if (!cacheConfigOptions.cacheNull()) {
                    customizedConfig.disableCachingNullValues();
                }
                if (!(cacheType).containsKey(name) && cacheConfigOptions.cacheType() != null && !cacheConfigOptions.cacheType().toString().equals(CacheType.ALL.name())) {
                    CacheType type = cacheConfigOptions.cacheType();
                    (cacheType).put(name, CacheType.valueOf(type.name()));
                }
                customizedConfig = invokeRedisCacheConfigCustomizer(name, customizedConfig, customizer);
                if (!redisCacheConfigs.containsKey(name)) {
                    redisCacheConfigs.put(name, customizedConfig);
                }
            }
        }
        properties.setType(cacheType);
    }

    public static void caffeinePatch(Map<String, Caffeine> caffeineMap, ApplicationContext applicationContext, CompositeCacheProperties properties) {
        Map<String, Object> customizedCacheAnnotationBeans = new HashMap<>();
        customizedCacheAnnotationBeans.putAll(applicationContext.getBeansWithAnnotation(CacheConfigOptions.class));
        customizedCacheAnnotationBeans.putAll(applicationContext.getBeansWithAnnotation(CacheConfigOptionsContainer.class));
        if (!customizedCacheAnnotationBeans.isEmpty()) {
            customizedCacheAnnotationBeans.forEach((key, value) -> {
                Class<?> targetClass = AopUtils.getTargetClass(value);
                CacheConfigOptions cacheConfigOptions = (CacheConfigOptions)AnnotationUtils.findAnnotation(targetClass, CacheConfigOptions.class);
                if (cacheConfigOptions != null) {
                    String[] cacheNames = cacheConfigOptions.cacheNames();
                    createCaffeine(caffeineMap, cacheConfigOptions, cacheNames, properties);
                }
                CacheConfigOptionsContainer cacheConfigOptionsContainer = AnnotationUtils.findAnnotation(targetClass, CacheConfigOptionsContainer.class);
                if (cacheConfigOptionsContainer != null) {
                    CacheConfigOptions[] options = cacheConfigOptionsContainer.value();
                    for (CacheConfigOptions option : options) {
                        createCaffeine(caffeineMap, option, option.cacheNames(), properties);
                    }
                }
            });
        }

    }

    private static void createCaffeine(Map<String, Caffeine> caffeineMap, CacheConfigOptions cacheConfigOptions, String[] cacheNames, CompositeCacheProperties properties) {
        Map<String, CacheType> cacheType = properties.getType();
        if (Objects.isNull(cacheType)) {
            cacheType = new HashMap<>();
        }
        for (String cacheName : cacheNames) {
            if (!caffeineMap.containsKey(cacheName)) {
                if (!StringUtils.isEmpty(cacheConfigOptions.spec())) {
                    caffeineMap.put(cacheName, Caffeine.from(cacheConfigOptions.spec()));
                } else {
                    int initialCapacity = cacheConfigOptions.initialCapacity();
                    long maximumSize = cacheConfigOptions.maximumSize();
                    String expireAfterWrite = cacheConfigOptions.expireAfterWrite();
                    String expireAfterAccess = cacheConfigOptions.expireAfterAccess();
                    Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
                    if (initialCapacity != -1) {
                        caffeine.initialCapacity(initialCapacity);
                    }
                    if (maximumSize != -1L) {
                        caffeine.maximumSize(maximumSize);
                    }
                    if (!StringUtils.isEmpty(expireAfterWrite)) {
                        caffeine.expireAfterWrite(DurationStyle.detectAndParse(expireAfterWrite));
                    }
                    if (!StringUtils.isEmpty(expireAfterAccess)) {
                        caffeine.expireAfterAccess(DurationStyle.detectAndParse(expireAfterAccess));
                    }
                    caffeineMap.put(cacheName, caffeine);
                }
                if (!(cacheType).containsKey(cacheName) && cacheConfigOptions.cacheType() != null && !cacheConfigOptions.cacheType().toString().equals(CacheType.ALL.name())) {
                    CacheType type = cacheConfigOptions.cacheType();
                    (cacheType).put(cacheName, CacheType.valueOf(type.name()));
                }
            }
        }
        properties.setType(cacheType);
    }

    public static RedisCacheConfiguration invokeRedisCacheConfigCustomizer(String cacheName, RedisCacheConfiguration config, RedisCacheConfigurationCustomizer customizer) {
        return customizer != null ? customizer.customize(cacheName, config) : config;
    }
}
