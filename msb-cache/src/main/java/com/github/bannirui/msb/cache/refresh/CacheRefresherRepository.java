package com.github.bannirui.msb.cache.refresh;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRefresherRepository {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefresherRepository.class);
    private static CopyOnWriteArrayList<CacheRefresher> cacheRefreshRepository = new CopyOnWriteArrayList<>();

    private static CacheRefresher queryByNameAndKey(String cacheName, String cacheKey) {
        return cacheRefreshRepository.stream()
            .filter((item) -> item.getCacheName().equals(cacheName) && item.getKey().equals(cacheKey))
            .findAny()
            .orElseGet(()->null);
    }

    public static void add(CacheRefresher cacheRefresher) {
        CacheRefresher refresher = queryByNameAndKey(cacheRefresher.getCacheName(), cacheRefresher.getKey().toString());
        if (Objects.nonNull(refresher)) {
            refresher.setCreatedTime(cacheRefresher.getCreatedTime());
        } else {
            cacheRefreshRepository.add(cacheRefresher);
        }
    }

    public static void remove(String cacheName, Object cacheKey) {
        cacheRefreshRepository.removeIf((item) -> item.getCacheName().equals(cacheName) && item.getKey().equals(cacheKey));
    }

    public static void clear(String cacheName) {
        cacheRefreshRepository.removeIf((item) -> item.getCacheName().equals(cacheName));
    }

    public static List<CacheRefresher> loadAll() {
        return cacheRefreshRepository;
    }
}
