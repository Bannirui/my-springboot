package com.github.bannirui.msb.cache.composite;

import com.github.bannirui.msb.cache.refresh.CacheRefresher;
import com.github.bannirui.msb.cache.refresh.CacheRefresherRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.springframework.cache.Cache;

public class CompositeCache implements Cache {
    private String name;
    private Cache local;
    private Cache remote;
    private boolean hasTtl;
    private boolean needRefresh;

    public CompositeCache(String name, Cache local, Cache remote) {
        this.name = name;
        this.local = local;
        this.remote = remote;
    }

    public CompositeCache(String name, Cache local, Cache remote, boolean hasTtl) {
        this.name = name;
        this.local = local;
        this.remote = remote;
        this.hasTtl = hasTtl;
    }

    public CompositeCache(String name, Cache local, Cache remote, boolean hasTtl, boolean needRefresh) {
        this.name = name;
        this.local = local;
        this.remote = remote;
        this.hasTtl = hasTtl;
        this.needRefresh = needRefresh;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        throw new UnsupportedOperationException("don't support native cache");
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = this.getReturnIfNotNull(this.local, (cache) -> cache.get(key));
        if (valueWrapper != null) {
            return valueWrapper;
        }
        valueWrapper = this.getReturnIfNotNull(this.remote, (cache) -> cache.get(key));
        if (valueWrapper != null) {
            Object val = valueWrapper.get();
            Optional.ofNullable(this.local).ifPresent((cache) -> {
                this.local.put(key, val);
            });
        }
        return valueWrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper valueWrapper = this.get(key);
        return valueWrapper == null ? null : (T) valueWrapper.get();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper valueWrapper = this.get(key);
        if(Objects.nonNull(valueWrapper)) {
            return (T) valueWrapper.get();
        }
        Object value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        this.put(key, value);
        return (T) value;
    }

    @Override
    public void put(Object key, Object value) {
        Optional.ofNullable(this.local).ifPresent((cache) -> {
            cache.put(key, value);
        });
        Optional.ofNullable(this.remote).ifPresent((cache) -> {
            cache.put(key, value);
        });
        if (!this.hasTtl && this.needRefresh) {
            CacheRefresherRepository.add(new CacheRefresher(this.name, key, System.currentTimeMillis()));
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existingValueWrapper = this.get(key);
        if (existingValueWrapper != null && existingValueWrapper.get() != null) {
            return existingValueWrapper;
        } else {
            this.put(key, value);
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        Optional.ofNullable(this.local).ifPresent((cache) -> {
            cache.evict(key);
        });
        Optional.ofNullable(this.remote).ifPresent((cache) -> {
            cache.evict(key);
        });
        if (this.needRefresh) {
            CacheRefresherRepository.remove(this.name, key);
        }

    }

    @Override
    public void clear() {
        Optional.ofNullable(this.local).ifPresent((cache) -> {
            cache.clear();
        });
        Optional.ofNullable(this.remote).ifPresent((cache) -> {
            cache.clear();
        });
        if (this.needRefresh) {
            CacheRefresherRepository.clear(this.name);
        }
    }

    private <R> R getReturnIfNotNull(Cache cache, Function<Cache, R> func) {
        return cache != null ? func.apply(cache) : null;
    }
}
