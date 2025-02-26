package com.github.bannirui.msb.endpoint.jmx;

import com.github.bannirui.msb.cache.redis.connection.DynamicJedisConnectionFactory;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Pool;

public class JedisDataMonitor implements MonitorForLogger, MonitorForCat, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JedisDataMonitor.class);
    private ApplicationContext applicationContext;
    private boolean hasDynamicJedisConnectionFactory = true;


    @Override
    public Map<String, String> monitor() {
        Map<String, RedisConnectionFactory> redisConnectionFactoryMap = this.applicationContext.getBeansOfType(RedisConnectionFactory.class);
        if(MapUtils.isEmpty(redisConnectionFactoryMap)) return null;
        for (Map.Entry<String, RedisConnectionFactory> entry : redisConnectionFactoryMap.entrySet()) {
                try {
                    try {
                        if (this.hasDynamicJedisConnectionFactory && entry.getValue() instanceof DynamicJedisConnectionFactory) {
                            return this.getPoolInfo(entry.getKey(), ((DynamicJedisConnectionFactory)entry.getValue()).getJedisConnectionFactory());
                        }
                    } catch (NoClassDefFoundError e) {
                        this.hasDynamicJedisConnectionFactory = false;
                    }
                    Object singletonTarget = AopProxyUtils.getSingletonTarget(entry.getValue());
                    if (singletonTarget == null) {
                        singletonTarget = entry.getValue();
                    }
                    if (singletonTarget instanceof JedisConnectionFactory) {
                        return this.getPoolInfo(entry.getKey(), (JedisConnectionFactory)singletonTarget);
                    }
                } catch (Exception e) {
                    logger.error("JMX 获取{} Jedis线程数据错误 errorMsg={}", entry.getKey(), e.getMessage());
                }
            }
        return null;
    }

    private Map<String, String> getPoolInfo(String factoryName, JedisConnectionFactory jedisConnectionFactory) throws IllegalAccessException {
        if (JedisConnectionFactory.class != jedisConnectionFactory.getClass()) {
            return Collections.emptyMap();
        }
        Map<String, String> monitorMap = new HashMap<>();
        this.getCluster(factoryName, jedisConnectionFactory, monitorMap);
        if (monitorMap.isEmpty()) {
            this.getStandaloneInfo(factoryName, jedisConnectionFactory, monitorMap);
        }
        return monitorMap;
    }

    private void getCluster(String factoryName, JedisConnectionFactory jedisConnectionFactory, Map<String, String> monitorMap) {
        Field clusterField = ReflectionUtils.findField(jedisConnectionFactory.getClass(), "cluster");
        if (clusterField != null) {
            ReflectionUtils.makeAccessible(clusterField);
            JedisCluster cluster = (JedisCluster) ReflectionUtils.getField(clusterField, jedisConnectionFactory);
            if(Objects.isNull(cluster)) return;
            Field connectionHandlerField = ReflectionUtils.findField(cluster.getClass(), "connectionHandler");
            if (connectionHandlerField == null) {
                return;
            }
            ReflectionUtils.makeAccessible(connectionHandlerField);
            // TODO: 2025/2/26
        }
    }

    private void getStandaloneInfo(String factoryName, JedisConnectionFactory jedisConnectionFactory, Map<String, String> monitorMap) throws IllegalAccessException {
        Field poolField = ReflectionUtils.findField(jedisConnectionFactory.getClass(), "pool");
        if (poolField != null) {
            ReflectionUtils.makeAccessible(poolField);
            Pool<Jedis> pool = (Pool)poolField.get(jedisConnectionFactory);
            if (pool != null) {
                monitorMap.put(String.format("%s.standalone.Active.count", factoryName), String.valueOf(pool.getNumActive()));
                monitorMap.put(String.format("%s.standalone.Idle.count", factoryName), String.valueOf(pool.getNumIdle()));
                monitorMap.put(String.format("%s.standalone.Awaiting.count", factoryName), String.valueOf(pool.getNumWaiters()));
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getId() {
        return "Jedis pool";
    }

    @Override
    public String getDescription() {
        return "Jedis连接池";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> monitorMap = this.monitor();
        if (monitorMap == null) {
            monitorMap = new HashMap<>();
        }
        return monitorMap;
    }
}
