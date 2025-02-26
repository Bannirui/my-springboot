package com.github.bannirui.msb.endpoint.jmx;

import com.github.bannirui.msb.cache.redis.connection.DynamicLettuceConnectionFactory;
import com.github.bannirui.msb.endpoint.util.RedisPoolReflectionUtils;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.util.ReflectionUtils;

public class LettuceDataMonitor implements MonitorForLogger, MonitorForCat, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(LettuceDataMonitor.class);
    private ApplicationContext applicationContext;
    private boolean hasDynamicLettuceConnectionFactory = true;
    private RedisPoolReflectionUtils redisPoolReflectionUtils = new RedisPoolReflectionUtils();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getId() {
        return "Lettuce pool";
    }

    @Override
    public String getDescription() {
        return "Lettuce监控信息";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> monitorMap = this.monitor();
        if (monitorMap == null) {
            monitorMap = new HashMap<>();
        }
        return monitorMap;
    }

    @Override
    public Map<String, String> monitor() {
        Map<String, RedisConnectionFactory> redisConnectionFactoryMap = this.applicationContext.getBeansOfType(RedisConnectionFactory.class);
        if (MapUtils.isEmpty(redisConnectionFactoryMap)) {
            return null;
        }
        Set<Map.Entry<String, RedisConnectionFactory>> redisConnectionFactorys = redisConnectionFactoryMap.entrySet();
        for (Map.Entry<String, RedisConnectionFactory> entry : redisConnectionFactorys) {
            String factoryBeanName = entry.getKey();
            RedisConnectionFactory redisConnectionFactory = entry.getValue();
            try {
                try {
                    if (this.hasDynamicLettuceConnectionFactory && redisConnectionFactory instanceof DynamicLettuceConnectionFactory) {
                        return this.getLettuceMonitorInfo(factoryBeanName, ((DynamicLettuceConnectionFactory) redisConnectionFactory).getLettuceConnectionFactory());
                    }
                } catch (NoClassDefFoundError e) {
                    this.hasDynamicLettuceConnectionFactory = false;
                }
                Object singletonTarget = AopProxyUtils.getSingletonTarget(redisConnectionFactory);
                if (singletonTarget == null) {
                    singletonTarget = redisConnectionFactory;
                }
                if (singletonTarget instanceof LettuceConnectionFactory) {
                    return this.getLettuceMonitorInfo(factoryBeanName, (LettuceConnectionFactory) singletonTarget);
                }
            } catch (Exception e) {
                logger.info("JMX 获取{} Lettuce线程数据错误 errorMsg={}", factoryBeanName, e.getMessage());
            }
        }
        return null;
    }

    private Map<String, String> getLettuceMonitorInfo(String factoryName, LettuceConnectionFactory lettuceConnectionFactory)
        throws IllegalAccessException {
        if (LettuceConnectionFactory.class != lettuceConnectionFactory.getClass()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> poolInfo = this.getPoolInfo(factoryName, lettuceConnectionFactory);
            this.lettuceThreadPoolMonitor(poolInfo, factoryName, lettuceConnectionFactory);
            return poolInfo;
        }
    }

    private Map<String, String> getPoolInfo(String factoryName, LettuceConnectionFactory lettuceConnectionFactory) throws IllegalAccessException {
        Map<String, String> monitorMap = new HashMap<>();
        Field connectionProviderField = ReflectionUtils.findField(LettuceConnectionFactory.class, "connectionProvider");
        if (connectionProviderField == null) {
            return monitorMap;
        }
        ReflectionUtils.makeAccessible(connectionProviderField);
        LettuceConnectionProvider connectionProvider = (LettuceConnectionProvider) connectionProviderField.get(lettuceConnectionFactory);
        Field poolsField = ReflectionUtils.findField(connectionProvider.getClass(), "pools");
        if (Objects.isNull(poolsField)) return monitorMap;
        ReflectionUtils.makeAccessible(poolsField);
        Map<Class<?>, GenericObjectPool<?>> pools = (Map) poolsField.get(connectionProvider);
        if(MapUtils.isEmpty(pools)) return monitorMap;
        for (Map.Entry<Class<?>, GenericObjectPool<?>> poolEntry : pools.entrySet()) {
            Class<?> key = poolEntry.getKey();
            GenericObjectPool<?> value = poolEntry.getValue();
            monitorMap.put(String.format("%s.%s.Active.count", factoryName, key.getSimpleName()), String.valueOf(value.getNumActive()));
            monitorMap.put(String.format("%s.%s.Idle.count", factoryName, key.getSimpleName()), Integer.toString(value.getNumIdle()));
            monitorMap.put(String.format("%s.%s.Awaiting.count", factoryName, key.getSimpleName()), Integer.toString(value.getNumWaiters()));
        }
        return monitorMap;
    }

    private void lettuceThreadPoolMonitor(Map<String, String> monitorMap, String factoryBeanName, LettuceConnectionFactory factory) {
        List<DefaultEventExecutor> defaultEventExecutors = this.redisPoolReflectionUtils.computationThreadPool(factory);
        if (defaultEventExecutors != null) {
            monitorMap.put(String.format("%s.lettuce.computationThreadPoolSize", factoryBeanName), "" + defaultEventExecutors.size());
            int sum = defaultEventExecutors.stream().mapToInt(SingleThreadEventExecutor::pendingTasks).sum();
            monitorMap.put(String.format("%s.lettuce.computationQueueSize", factoryBeanName), "" + sum);
        }
        List<SingleThreadEventExecutor> nioEventLoops = this.redisPoolReflectionUtils.ioThreadPool(factory);
        if (nioEventLoops != null) {
            monitorMap.put(String.format("%s.lettuce.ioThreadPoolSize", factoryBeanName), "" + nioEventLoops.size());
            int sum = nioEventLoops.stream().mapToInt(SingleThreadEventExecutor::pendingTasks).sum();
            monitorMap.put(String.format("%s.lettuce.ioThreadQueueSize", factoryBeanName), "" + sum);
        }
    }
}
