package com.github.bannirui.msb.cache.refresh;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CacheRefreshTask implements InitializingBean, DisposableBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshTask.class);
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private CompositeCacheProperties cacheProperties;
    @Value("${cache.refresh-delay-seconds:300s}")
    private Duration refreshDelaySeconds;
    @Value("${cache.time-between-refresh-runs-seconds:86400s}")
    private Duration timeBetweenRefreshRunsSeconds;
    private ApplicationContext applicationContext;
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    @Override
    public void afterPropertiesSet() {
        Map<String, CacheLoader> cacheLoaderMap = this.applicationContext.getBeansOfType(CacheLoader.class);
        this.ses.scheduleAtFixedRate(() -> {
            for (CacheRefresher refresher : CacheRefresherRepository.loadAll()) {
                if (System.currentTimeMillis() - refresher.getCreatedTime() > this.cacheProperties.getRefreshInterval().toMillis()) {
                    Cache cache = this.cacheManager.getCache(refresher.getCacheName());
                    CacheLoader cacheLoader = cacheLoaderMap.get(refresher.getCacheName());
                    if (cacheLoader != null) {
                        CompletableFuture.supplyAsync(() -> cacheLoader.load(refresher.getKey().toString())).thenAccept((result) -> {
                            cache.put(refresher.getKey().toString(), result);
                        });
                    } else {
                        CacheRefreshTask.logger.error("开启自动刷新缓存功能时 必须提供相应的CacheLoader实现 当前cacheName:{}", refresher.getCacheName());
                    }
                }
            }
        }, this.refreshDelaySeconds.getSeconds(), this.timeBetweenRefreshRunsSeconds.getSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(this.ses)) {
            this.ses.shutdown();
        }
    }
}
