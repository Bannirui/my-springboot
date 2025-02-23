package com.github.bannirui.msb.cache.redis.connection;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class DynamicJedisConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(DynamicJedisConnectionFactory.class);
    private AtomicReference<JedisConnectionFactory> connectionFactoryAtomicReference;

    public DynamicJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory) {
        this.connectionFactoryAtomicReference = new AtomicReference(jedisConnectionFactory);
    }

    public JedisConnectionFactory setConnectionFactory(JedisConnectionFactory newConnectionFactory) {
        newConnectionFactory.afterPropertiesSet();
        return this.connectionFactoryAtomicReference.getAndSet(newConnectionFactory);
    }

    @Override
    public void destroy() {
        this.connectionFactoryAtomicReference.get().destroy();
        logger.info("DynamicJedisConnectionFactory {} has been destroyed.", this.connectionFactoryAtomicReference.get());
    }

    @Override
    public void afterPropertiesSet() {
        this.connectionFactoryAtomicReference.get().afterPropertiesSet();
        logger.info("DynamicJedisConnectionFactory {} has been initialized.", this.connectionFactoryAtomicReference.get());
    }

    @Override
    public RedisConnection getConnection() {
        return this.connectionFactoryAtomicReference.get().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return this.connectionFactoryAtomicReference.get().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return this.connectionFactoryAtomicReference.get().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return this.connectionFactoryAtomicReference.get().getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        return this.connectionFactoryAtomicReference.get().translateExceptionIfPossible(e);
    }

    public JedisConnectionFactory getJedisConnectionFactory() {
        return this.connectionFactoryAtomicReference.get();
    }
}
