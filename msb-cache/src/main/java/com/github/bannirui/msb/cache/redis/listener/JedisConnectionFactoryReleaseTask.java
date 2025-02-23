package com.github.bannirui.msb.cache.redis.listener;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.util.Pool;

public class JedisConnectionFactoryReleaseTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JedisConnectionFactoryReleaseTask.class);
    private final JedisConnectionFactory jedisConnectionFactory;
    private final ScheduledExecutorService scheduledExecutorService;
    private int RETRY_DELAY_IN_MILLISECONDS = 5000;
    private static final int GAP_IN_MILLISECONDS = 5000;
    private static final int MAX_RETRY_TIMES = 10;
    private volatile int retry = 0;

    public JedisConnectionFactoryReleaseTask(JedisConnectionFactory jedisConnectionFactory, ScheduledExecutorService scheduledExecutorService) {
        this.jedisConnectionFactory = jedisConnectionFactory;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        if (this.release(this.jedisConnectionFactory)) {
            logger.info("jedis connection factory {} released successfully!", this.jedisConnectionFactory);
        } else if (this.retry < 10) {
            this.RETRY_DELAY_IN_MILLISECONDS += 5000;
            this.scheduledExecutorService.schedule(this, (long)this.RETRY_DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("releasing retry time has reached max. {} release failed. force to release jedis connection.", this.jedisConnectionFactory);
            this.jedisConnectionFactory.destroy();
        }
    }

    private boolean release(JedisConnectionFactory jedisConnectionFactory) {
        logger.info("try to release jedis connection factory : {}", jedisConnectionFactory);
        boolean ret=false;
        try {
            Field poolField = ReflectionUtils.findField(jedisConnectionFactory.getClass(), "pool");
            ReflectionUtils.makeAccessible(poolField);
            Pool pool = (Pool)ReflectionUtils.getField(poolField, jedisConnectionFactory);
            if (pool != null && pool.getNumActive() > 0) {
                this.RETRY_DELAY_IN_MILLISECONDS += 5000;
                this.scheduledExecutorService.schedule(this, (long)this.RETRY_DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
            } else {
                jedisConnectionFactory.destroy();
            }
            return true;
        } catch (Exception e) {
            logger.error("release jedis connection factory error.", e);
            ret = false;
        } finally {
            ++this.retry;
        }
        return ret;
    }
}
