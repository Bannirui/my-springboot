package com.github.bannirui.msb.endpoint.util;

import io.lettuce.core.AbstractRedisClient;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Pool;

public class RedisPoolReflectionUtils {

    public Map<String, Pool<?>> reflectPoolInfo(JedisConnectionFactory factory) {
        Field clusterField = ReflectionUtils.findField(factory.getClass(), "cluster");
        ReflectionUtils.makeAccessible(clusterField);
        JedisCluster jedisCluster = (JedisCluster)ReflectionUtils.getField(clusterField, factory);
        Field poolField;
        if (jedisCluster != null) {
            poolField = ReflectionUtils.findField(jedisCluster.getClass(), "connectionHandler");
            ReflectionUtils.makeAccessible(poolField);
            // TODO: 2025/2/26
            return Collections.unmodifiableMap(new HashMap<>());
        } else {
            poolField = ReflectionUtils.findField(factory.getClass(), "pool");
            ReflectionUtils.makeAccessible(poolField);
            Pool<?> pool = (Pool)ReflectionUtils.getField(poolField, factory);
            String key = factory.getHostName() + ":" + factory.getPort();
            HashMap<String, Pool<?>> nodes = new HashMap<>();
            nodes.put(key, pool);
            return Collections.unmodifiableMap(nodes);
        }
    }

    public Map<Class<?>, GenericObjectPool<?>> reflectPoolInfo(LettuceConnectionFactory factory) {
        Field connectionProviderField = ReflectionUtils.findField(factory.getClass(), "connectionProvider");
        ReflectionUtils.makeAccessible(connectionProviderField);
        Object object = ReflectionUtils.getField(connectionProviderField, factory);
        Field poolsField = ReflectionUtils.findField(object.getClass(), "pools");
        if (poolsField != null) {
            ReflectionUtils.makeAccessible(poolsField);
            Object poolsObj = ReflectionUtils.getField(poolsField, object);
            if (poolsObj instanceof Map) {
                return (Map)poolsObj;
            }
        }
        return Collections.emptyMap();
    }

    public List<DefaultEventExecutor> computationThreadPool(LettuceConnectionFactory factory) {
        Field clientField = ReflectionUtils.findField(factory.getClass(), "client");
        ReflectionUtils.makeAccessible(clientField);
        AbstractRedisClient redisClient = (AbstractRedisClient)ReflectionUtils.getField(clientField, factory);
        Field genericWorkerPoolField = ReflectionUtils.findField(redisClient.getClass(), "genericWorkerPool");
        ReflectionUtils.makeAccessible(genericWorkerPoolField);
        DefaultEventExecutorGroup genericWorkerPool = (DefaultEventExecutorGroup)ReflectionUtils.getField(genericWorkerPoolField, redisClient);
        if (genericWorkerPool == null) {
            return null;
        }
        List<DefaultEventExecutor> objects = new ArrayList<>();
        for (EventExecutor eventExecutor : genericWorkerPool) {
            objects.add((DefaultEventExecutor)eventExecutor);
        }
        return objects;
    }

    public List<SingleThreadEventExecutor> ioThreadPool(LettuceConnectionFactory factory) {
        Field clientField = ReflectionUtils.findField(factory.getClass(), "client");
        ReflectionUtils.makeAccessible(clientField);
        AbstractRedisClient redisClient = (AbstractRedisClient)ReflectionUtils.getField(clientField, factory);
        Field eventLoopGroupsField = ReflectionUtils.findField(redisClient.getClass(), "eventLoopGroups");
        ReflectionUtils.makeAccessible(eventLoopGroupsField);
        Map<Class<? extends EventLoopGroup>, EventLoopGroup> eventLoopGroups = (Map)ReflectionUtils.getField(eventLoopGroupsField, redisClient);
        if(MapUtils.isEmpty(eventLoopGroups)) return null;
        List<SingleThreadEventExecutor> objects = new ArrayList<>();
        for (EventLoopGroup executor : eventLoopGroups.values()) {
            objects.add((SingleThreadEventExecutor)executor);
        }
        return objects;
    }
}
