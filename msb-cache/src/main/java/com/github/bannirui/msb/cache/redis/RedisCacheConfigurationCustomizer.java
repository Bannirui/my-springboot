package com.github.bannirui.msb.cache.redis;

import org.springframework.data.redis.cache.RedisCacheConfiguration;

public interface RedisCacheConfigurationCustomizer {
    RedisCacheConfiguration customize(String cacheName, RedisCacheConfiguration cacheConfig);
}
