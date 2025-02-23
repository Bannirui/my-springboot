package com.github.bannirui.msb.cache.refresh;

import java.time.Duration;

public class CacheRefresher {
    private String cacheName;
    private Object key;
    private long createdTime;
    /** @deprecated */
    @Deprecated
    private Duration refreshInterval;

    public CacheRefresher(String cacheName, Object key, long createdTime) {
        this.cacheName = cacheName;
        this.key = key;
        this.createdTime = createdTime;
    }

    /** @deprecated */
    @Deprecated
    public CacheRefresher(String cacheName, String key, long createdTime, Duration refreshInterval) {
        this.cacheName = cacheName;
        this.key = key;
        this.createdTime = createdTime;
        this.refreshInterval = refreshInterval;
    }

    public String getCacheName() {
        return this.cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /** @deprecated */
    @Deprecated
    public Duration getRefreshInterval() {
        return this.refreshInterval;
    }

    /** @deprecated */
    @Deprecated
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Object getKey() {
        return this.key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public long getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
