package com.github.bannirui.msb.cache.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class CaffeineCacheEnhanceManager extends AbstractCacheManager {
    private Map<String, Caffeine> initialCacheConfiguration;
    private boolean allowNullValues = true;
    private Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
    private CacheLoader<Object, Object> cacheLoader;

    public CaffeineCacheEnhanceManager(Map<String, Caffeine> initialCacheConfiguration) {
        this.initialCacheConfiguration = initialCacheConfiguration;
    }

    public void setCaffeine(Caffeine<Object, Object> caffeine) {
        Assert.notNull(caffeine, "Caffeine must not be null");
        this.doSetCaffeine(caffeine);
    }

    public void setCaffeineSpec(CaffeineSpec caffeineSpec) {
        this.doSetCaffeine(Caffeine.from(caffeineSpec));
    }

    public void setCaffeineSpecification(String caffeineSpecification) {
        this.doSetCaffeine(Caffeine.from(caffeineSpecification));
    }

    private void doSetCaffeine(Caffeine<Object, Object> caffeine) {
        if (!ObjectUtils.nullSafeEquals(this.caffeine, caffeine)) {
            this.caffeine = caffeine;
        }
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        List<Cache> caches = new LinkedList<>();
        this.initialCacheConfiguration.entrySet().stream().forEach((entry) -> {
            caches.add(this.createCaffeineCache(entry.getKey(), entry.getValue()));
        });
        return caches;
    }

    protected CaffeineCache createCaffeineCache(String name, Caffeine caffeine) {
        return new CaffeineCache(name, this.createNativeCaffeineCache(caffeine != null ? caffeine : this.caffeine), this.isAllowNullValues());
    }

    protected com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCache(Caffeine caffeine) {
        return (this.cacheLoader != null ? caffeine.build(this.cacheLoader) : caffeine.build());
    }

    @Override
    protected Cache getMissingCache(String name) {
        return this.createCaffeineCache(name, null);
    }

    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public void setCacheLoader(CacheLoader<Object, Object> cacheLoader) {
        if (!ObjectUtils.nullSafeEquals(this.cacheLoader, cacheLoader)) {
            this.cacheLoader = cacheLoader;
        }
    }
}
