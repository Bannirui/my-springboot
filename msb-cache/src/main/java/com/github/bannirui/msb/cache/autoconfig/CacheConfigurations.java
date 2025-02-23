package com.github.bannirui.msb.cache.autoconfig;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.util.Assert;

final class CacheConfigurations {
    private static final Map<CacheType, Class<?>> MAPPINGS;

    static {
        Map<CacheType, Class<?>> mappings = new EnumMap(CacheType.class);
        mappings.put(CacheType.CAFFEINE, CaffeineCacheConfiguration.class);
        mappings.put(CacheType.REDIS, RedisCacheConfig.class);
        MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    public static String getConfigurationClass(CacheType cacheType) {
        Class<?> configurationClass = MAPPINGS.get(cacheType);
        Assert.state(configurationClass != null, () -> "Unknown cache type" + cacheType);
        return configurationClass.getName();
    }
}
