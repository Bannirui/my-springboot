package com.github.bannirui.msb.cache.refresh;

public interface CacheLoader<T> {
    T load(String key);
}
