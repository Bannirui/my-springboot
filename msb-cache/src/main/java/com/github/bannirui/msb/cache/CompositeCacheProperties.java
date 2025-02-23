package com.github.bannirui.msb.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "cache"
)
public class CompositeCacheProperties {
    private boolean allowNullValues = true;
    private Duration refreshInterval;
    private Map<String, CacheType> type = new HashMap<>();
    private Map<String, CacheProperties.Caffeine> caffeine = new HashMap<>();
    private Map<String, CacheProperties.Redis> redis = new HashMap<>();

    public Map<String, CacheProperties.Caffeine> getCaffeine() {
        return this.caffeine;
    }

    public void setCaffeine(Map<String, CacheProperties.Caffeine> caffeine) {
        this.caffeine = caffeine;
    }

    public Map<String, CacheProperties.Redis> getRedis() {
        return this.redis;
    }

    public void setRedis(Map<String, CacheProperties.Redis> redis) {
        this.redis = redis;
    }

    public Map<String, CacheType> getType() {
        return this.type;
    }

    public void setType(Map<String, CacheType> type) {
        this.type = type;
    }

    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public Duration getRefreshInterval() {
        return this.refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
