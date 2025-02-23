package com.github.bannirui.msb.cache.autoconfig;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.caffeine.CaffeineCacheEnhanceManager;
import com.github.bannirui.msb.cache.util.PatchUtil;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({Caffeine.class, CaffeineCacheEnhanceManager.class})
@ConditionalOnMissingBean({CacheManager.class})
public class CaffeineCacheConfiguration implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineCacheConfiguration.class);
    private CacheProperties cacheProperties;
    private CompositeCacheProperties compositeCacheProperties;
    private final Caffeine<Object, Object> caffeine;
    private final CaffeineSpec caffeineSpec;
    private final CacheLoader cacheLoader;
    private final CacheManagerCustomizers customizers;
    private ApplicationContext applicationContext;

    public CaffeineCacheConfiguration(CacheProperties cacheProperties, CompositeCacheProperties compositeCacheProperties, CacheManagerCustomizers customizers, ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec, ObjectProvider<CacheLoader<Object, Object>> cacheLoader, ApplicationContext applicationContext) {
        this.cacheProperties = cacheProperties;
        this.compositeCacheProperties = compositeCacheProperties;
        this.customizers = customizers;
        this.caffeine = caffeine.getIfAvailable();
        this.caffeineSpec = caffeineSpec.getIfAvailable();
        this.cacheLoader = cacheLoader.getIfAvailable();
        this.applicationContext = applicationContext;
    }

    @Bean
    public CacheManager cacheManager(CompositeCacheProperties properties) {
        Map<String, Caffeine> caffeineMap = new HashMap<>();
        Map<String, CacheProperties.Caffeine> caffeineProperties = properties.getCaffeine();
        if (caffeineProperties != null) {
            caffeineProperties.entrySet().stream().forEach((entry) -> {
                caffeineMap.put(entry.getKey(), Caffeine.from(entry.getValue().getSpec()));
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
        CaffeineCacheEnhanceManager customize = this.customizers.customize(cacheManager);
        logger.info("CaffeineCacheEnhanceManager {} has created.", customize);
        return customize;
    }

    private CaffeineCacheEnhanceManager createCaffeineManager(Map<String, Caffeine> caffeineMap) {
        CaffeineCacheEnhanceManager cacheManager = new CaffeineCacheEnhanceManager(caffeineMap);
        if (StringUtils.isEmpty(this.cacheProperties.getCaffeine().getSpec())) {
            logger.warn("caffeine spec in CacheProperties is not configured.");
        }
        if (!StringUtils.isEmpty(this.cacheProperties.getCaffeine().getSpec())) {
            cacheManager.setCaffeineSpecification(this.cacheProperties.getCaffeine().getSpec());
        } else if (this.caffeineSpec != null) {
            cacheManager.setCaffeineSpec(this.caffeineSpec);
        } else if (this.caffeine != null) {
            cacheManager.setCaffeine(this.caffeine);
        }
        return cacheManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
