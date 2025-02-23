package com.github.bannirui.msb.cache.redis.listener;

import io.lettuce.core.AbstractRedisClient;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.ClusterCommandExecutor;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.util.ReflectionUtils;

public class LettuceConnectionFactoryReleaseTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LettuceConnectionFactoryReleaseTask.class);
    private final LettuceConnectionFactory lettuceConnectionFactory;
    private final ScheduledExecutorService scheduledExecutorService;
    private int RETRY_DELAY_IN_MILLISECONDS = 5000;
    private static final int GAP_IN_MILLISECONDS = 5000;
    private static final int MAX_RETRY_TIMES = 10;
    private volatile int retry = 0;

    public LettuceConnectionFactoryReleaseTask(LettuceConnectionFactory lettuceConnectionFactory, ScheduledExecutorService scheduledExecutorService) {
        this.lettuceConnectionFactory = lettuceConnectionFactory;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        if (this.release(this.lettuceConnectionFactory)) {
            logger.info("lettuce connection factory {} released successfully!", this.lettuceConnectionFactory);
        } else if (this.retry < 10) {
            this.RETRY_DELAY_IN_MILLISECONDS += 5000;
            this.scheduledExecutorService.schedule(this, this.RETRY_DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("releasing retry time has reached max. {} release failed. force to release lettuce connection.", this.lettuceConnectionFactory);
            this.lettuceConnectionFactory.destroy();
        }
    }

    private boolean release(LettuceConnectionFactory lettuceConnectionFactory) {
        logger.info("try to release lettuce connection factory : {}", lettuceConnectionFactory);
        boolean ret=false;
        try {
            lettuceConnectionFactory.resetConnection();
            this.releaseConnectionProvider(lettuceConnectionFactory, "connectionProvider");
            this.releaseConnectionProvider(lettuceConnectionFactory, "reactiveConnectionProvider");
            this.releaseClient(lettuceConnectionFactory);
            this.releaseClusterCommandExecutor(lettuceConnectionFactory);
            return true;
        } catch (Exception e) {
            logger.error("release lettuce connection factory error.", e);
            ret = false;
        } finally {
            ++this.retry;
        }
        return ret;
    }

    private boolean releaseConnectionProvider(LettuceConnectionFactory lettuceConnectionFactory, String connectionProviderName) throws Exception {
        Field connectionProviderField = ReflectionUtils.findField(LettuceConnectionFactory.class, connectionProviderName);
        ReflectionUtils.makeAccessible(connectionProviderField);
        LettuceConnectionProvider connectionProvider = (LettuceConnectionProvider)connectionProviderField.get(lettuceConnectionFactory);
        if (connectionProvider instanceof DisposableBean) {
            try {
                ((DisposableBean)connectionProvider).destroy();
            } catch (Exception e) {
                logger.warn(connectionProviderName + " did not shut down gracefully.", e);
                throw e;
            }
        }
        return true;
    }

    private boolean releaseClient(LettuceConnectionFactory lettuceConnectionFactory) {
        Field clientConfigurationField = ReflectionUtils.findField(lettuceConnectionFactory.getClass(), "clientConfiguration");
        ReflectionUtils.makeAccessible(clientConfigurationField);
        LettuceClientConfiguration clientConfiguration = (LettuceClientConfiguration)ReflectionUtils.getField(clientConfigurationField, lettuceConnectionFactory);
        Field clientField = ReflectionUtils.findField(lettuceConnectionFactory.getClass(), "client");
        ReflectionUtils.makeAccessible(clientField);
        AbstractRedisClient client = (AbstractRedisClient)ReflectionUtils.getField(clientField, lettuceConnectionFactory);
        Duration timeout = clientConfiguration.getShutdownTimeout();
        client.shutdown(timeout.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean releaseClusterCommandExecutor(LettuceConnectionFactory lettuceConnectionFactory) throws Exception {
        Field clusterCommandExecutorField = ReflectionUtils.findField(lettuceConnectionFactory.getClass(), "clusterCommandExecutor");
        ReflectionUtils.makeAccessible(clusterCommandExecutorField);
        ClusterCommandExecutor clusterCommandExecutor = (ClusterCommandExecutor)ReflectionUtils.getField(clusterCommandExecutorField, lettuceConnectionFactory);
        if (clusterCommandExecutor != null) {
            clusterCommandExecutor.destroy();
        }
        return true;
    }
}
