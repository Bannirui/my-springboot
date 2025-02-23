package com.github.bannirui.msb.cache.redis.connection;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class DynamicLettuceConnectionFactory implements RedisConnectionFactory, ReactiveRedisConnectionFactory, InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLettuceConnectionFactory.class);
    private AtomicReference<LettuceConnectionFactory> connectionFactoryAtomicReference;

    public DynamicLettuceConnectionFactory(LettuceConnectionFactory connectionFactory) {
        this.connectionFactoryAtomicReference = new AtomicReference(connectionFactory);
    }

    public LettuceConnectionFactory setConnectionFactory(LettuceConnectionFactory newConnectionFactory) {
        newConnectionFactory.afterPropertiesSet();
        return (LettuceConnectionFactory)this.connectionFactoryAtomicReference.getAndSet(newConnectionFactory);
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

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        return this.connectionFactoryAtomicReference.get().getReactiveConnection();
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        return this.connectionFactoryAtomicReference.get().getReactiveClusterConnection();
    }

    @Override
    public void destroy() {
        this.connectionFactoryAtomicReference.get().destroy();
        logger.info("DynamicLettuceConnectionFactory {} has been destroyed.", this.connectionFactoryAtomicReference.get());
    }

    @Override
    public void afterPropertiesSet() {
        this.connectionFactoryAtomicReference.get().afterPropertiesSet();
        logger.info("DynamicLettuceConnectionFactory {} has been initialized.", this.connectionFactoryAtomicReference.get());
    }

    public LettuceConnectionFactory getLettuceConnectionFactory() {
        return this.connectionFactoryAtomicReference.get();
    }
}
