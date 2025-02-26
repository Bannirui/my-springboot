package com.github.bannirui.msb.endpoint.health;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;

public class RedisHealthIndicator implements HealthIndicator {
    public static final Logger logger = LoggerFactory.getLogger(RedisHealthIndicator.class);
    static final String VERSION = "version";
    static final String REDIS_VERSION = "redis_version";
    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.redisConnectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        Health health = Health.build();
        this.doHealthCheck(health);
        return health;
    }

    protected void doHealthCheck(Health health) {
        RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
        try {
            if (connection instanceof RedisClusterConnection) {
                ClusterInfo clusterInfo = ((RedisClusterConnection)connection).clusterGetClusterInfo();
                health.up().withDetail("cluster_size", String.valueOf(clusterInfo.getClusterSize())).withDetail("slots_up", String.valueOf(clusterInfo.getSlotsOk())).withDetail("slots_fail", String.valueOf(clusterInfo.getSlotsFail()));
            } else {
                Properties info = connection.info();
                health.up().withDetail("version", info.getProperty("redis_version"));
            }
        } catch (Exception e) {
            logger.error("redisHealthIndicator", e);
            health.down(e.getClass().getName() + ": " + e.getMessage());
        } finally {
            RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
        }
    }
}
