package com.github.bannirui.msb.cache.redis.lock;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisLock implements Lock {
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);
    private int expireMsecs = 60000;
    private String lockKey;
    private boolean locked = false;
    private String currentLockValue;
    private RedisTemplate<String, String> redisTemplate;

    public RedisLock(RedisTemplate<String, String> redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = "lock_" + lockKey;
    }

    @Override
    public void lock() {
        while(true) {
            long expires = this.getCurrentTimeFromRedis() + (long)this.expireMsecs + 1L;
            String expiresStr = String.valueOf(expires);
            if (this.setNX(this.lockKey, expiresStr)) {
                this.currentLockValue = expiresStr;
                this.locked = true;
                return;
            }
            if (this.getLock(expiresStr)) {
                return;
            }
            try {
                Random random = new Random();
                int sleepTime = random.nextInt(100) % 51 + 50;
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                logger.error("延迟获取锁失败: " + e);
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
    }

    @Override
    public boolean tryLock() {
        long expires = this.getCurrentTimeFromRedis() + (long)this.expireMsecs + 1L;
        String expiresStr = String.valueOf(expires);
        if (this.setNX(this.lockKey, expiresStr)) {
            this.currentLockValue = expiresStr;
            this.locked = true;
            return true;
        } else {
            return this.getLock(expiresStr);
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long timeout = unit.toMillis(time);
        while(timeout >= 0L) {
            long expires = this.getCurrentTimeFromRedis() + (long)this.expireMsecs + 1L;
            String expiresStr = String.valueOf(expires);
            if (this.setNX(this.lockKey, expiresStr)) {
                this.currentLockValue = expiresStr;
                this.locked = true;
                return true;
            }
            if (this.getLock(expiresStr)) {
                return true;
            }
            Random random = new Random();
            int sleepTime = random.nextInt(100) % 51 + 50;
            timeout -= sleepTime;
            Thread.sleep(sleepTime);
        }
        return false;
    }

    @Override
    public void unlock() {
        String redisLockedValueStr = this.get(this.lockKey);
        if (this.locked) {
            this.locked = false;
            if (this.currentLockValue.equals(redisLockedValueStr)) {
                this.redisTemplate.delete(this.lockKey);
            }
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    private boolean getLock(String expiresStr) {
        this.currentLockValue = this.get(this.lockKey);
        if (this.currentLockValue != null && Long.parseLong(this.currentLockValue) < this.getCurrentTimeFromRedis()) {
            String oldValueStr = this.getSet(this.lockKey, expiresStr);
            if (oldValueStr != null && oldValueStr.equals(this.currentLockValue)) {
                this.currentLockValue = expiresStr;
                this.locked = true;
                return true;
            }
        }
        return false;
    }

    private String get(final String key) {
        Object obj = null;
        try {
            obj = this.redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("get redis error, key : {}", key, e);
        }
        return obj != null ? obj.toString() : null;
    }

    private boolean setNX(final String key, final String value) {
        Boolean obj = null;
        try {
            obj = this.redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            logger.error("setNX redis error, key : {}", key, e);
        }
        return obj != null ? obj : false;
    }

    private String getSet(final String key, final String value) {
        Object obj = null;
        try {
            obj = this.redisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            logger.error("setNX redis error, key : {}", key, e);
        }
        return obj != null ? (String)obj : null;
    }

    private long getCurrentTimeFromRedis() {
        Long obj = null;
        try {
            obj = this.redisTemplate.execute((RedisCallback<Long>) connection -> connection.time());
        } catch (Exception e) {
            logger.error("get redis time error ", e);
        }
        return obj != null ? obj : null;
    }
}
