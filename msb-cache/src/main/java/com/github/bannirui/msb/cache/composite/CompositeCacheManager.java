package com.github.bannirui.msb.cache.composite;

import com.github.bannirui.msb.cache.CacheType;
import com.github.bannirui.msb.cache.CompositeCacheProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;

public class CompositeCacheManager extends AbstractCacheManager {
    private CacheManager local;
    private CacheManager remote;
    private CompositeCacheProperties properties;
    private CacheProperties cacheProperties;
    private Map<String, Boolean> ttlHolder = new HashMap<>();

    public CompositeCacheManager(CacheManager local, CacheManager remote, CompositeCacheProperties properties, CacheProperties cacheProperties) {
        this.local = local;
        this.remote = remote;
        this.properties = properties;
        this.cacheProperties = cacheProperties;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return Collections.emptyList();
    }

    @Override
    protected Cache getMissingCache(String name) {
        boolean hasTtl = true;
        if (CacheType.ONLY_L1.equals(this.properties.getType().get(name))) {
            if (this.ttlHolder.containsKey(name)) {
                hasTtl = this.ttlHolder.get(name);
            } else if (Objects.nonNull(this.properties.getCaffeine().get(name))) {
                if (Objects.nonNull((this.properties.getCaffeine().get(name)).getSpec()) && !(this.properties.getCaffeine().get(name)).getSpec().contains("expire")) {
                    hasTtl = false;
                    this.ttlHolder.put(name, false);
                }
            } else if (Objects.nonNull(this.cacheProperties.getCaffeine()) && Objects.nonNull(this.cacheProperties.getCaffeine().getSpec()) && !this.cacheProperties.getCaffeine().getSpec().contains("expire")) {
                hasTtl = false;
                this.ttlHolder.put(name, false);
            }
            return new CompositeCache(name, this.local.getCache(name), (Cache)null, hasTtl, this.properties.getRefreshInterval() != null);
        } else if (CacheType.ONLY_L2.equals(this.properties.getType().get(name))) {
            if (this.ttlHolder.containsKey(name)) {
                hasTtl = this.ttlHolder.get(name);
            } else if (Objects.nonNull(this.properties.getRedis().get(name)) && this.properties.getRedis().get(name).getTimeToLive().isZero()) {
                hasTtl = false;
                this.ttlHolder.put(name, false);
            } else if (Objects.nonNull(this.cacheProperties.getRedis().getTimeToLive()) && this.cacheProperties.getRedis().getTimeToLive().isZero()) {
                hasTtl = false;
                this.ttlHolder.put(name, false);
            }
            return new CompositeCache(name, null, this.remote.getCache(name), hasTtl, this.properties.getRefreshInterval() != null);
        } else {
            if (this.ttlHolder.containsKey(name)) {
                hasTtl = this.ttlHolder.get(name);
            } else if ((!Objects.nonNull(this.properties.getCaffeine().get(name)) || !Objects.nonNull((this.properties.getCaffeine().get(name)).getSpec()) || (this.properties.getCaffeine().get(name)).getSpec().contains("expire")) && (!Objects.nonNull(this.properties.getRedis().get(name)) || !this.properties.getRedis().get(name)
                .getTimeToLive().isZero())) {
                if (Objects.nonNull(this.cacheProperties.getCaffeine()) && Objects.nonNull(this.cacheProperties.getCaffeine().getSpec()) && !this.cacheProperties.getCaffeine().getSpec().contains("expire") || Objects.nonNull(this.cacheProperties.getRedis().getTimeToLive()) && this.cacheProperties.getRedis().getTimeToLive().isZero()) {
                    hasTtl = false;
                    this.ttlHolder.put(name, false);
                }
            } else {
                hasTtl = false;
                this.ttlHolder.put(name, false);
            }
            Cache l1 = this.local.getCache(name);
            Cache l2 = this.remote.getCache(name);
            return l1 == null && l2 == null ? null : new CompositeCache(name, l1, l2, hasTtl, this.properties.getRefreshInterval() != null);
        }
    }
}
