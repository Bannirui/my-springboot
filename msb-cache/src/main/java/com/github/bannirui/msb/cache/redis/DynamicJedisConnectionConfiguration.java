package com.github.bannirui.msb.cache.redis;

import com.github.bannirui.msb.cache.redis.connection.DynamicJedisConnectionFactory;
import com.github.bannirui.msb.cache.util.PoolConfigUtil;
import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

@Configuration
@ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class})
@ConditionalOnProperty(
    name = {"spring.redis.client-type"},
    havingValue = "jedis",
    matchIfMissing = true
)
public class DynamicJedisConnectionConfiguration extends RedisConnectionConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DynamicJedisConnectionConfiguration.class);
    private RedisProperties redisProperties;
    private final ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers;

    public DynamicJedisConnectionConfiguration(RedisProperties redisProperties, ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider, ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
        super(sentinelConfigurationProvider, clusterConfigurationProvider);
        this.redisProperties = redisProperties;
        this.builderCustomizers = builderCustomizers;
    }

    @Bean({"dynamicRedisConnectionFactory"})
    @ConditionalOnMissingBean({RedisConnectionFactory.class})
    public RedisConnectionFactory redisConnectionFactory() {
        DynamicJedisConnectionFactory factory = new DynamicJedisConnectionFactory(this.createJedisConnectionFactory(this.redisProperties));
        logger.info("DynamicJedisConnectionFactory {} has created.", factory);
        return factory;
    }

    public JedisConnectionFactory createJedisConnectionFactory(RedisProperties redisProperties) {
        JedisClientConfiguration clientConfiguration = this.getJedisClientConfiguration(redisProperties);
        if (this.getSentinelConfig(redisProperties) != null) {
            return new JedisConnectionFactory(this.getSentinelConfig(redisProperties), clientConfiguration);
        } else {
            return this.getClusterConfiguration(redisProperties) != null ? new JedisConnectionFactory(this.getClusterConfiguration(redisProperties), clientConfiguration) : new JedisConnectionFactory(this.getStandaloneConfig(redisProperties), clientConfiguration);
        }
    }

    private JedisClientConfiguration getJedisClientConfiguration(RedisProperties redisProperties) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = this.applyProperties(JedisClientConfiguration.builder());
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();
        if (pool != null) {
            this.applyPooling(pool, builder);
        }
        if (StringUtils.isNotBlank(redisProperties.getUrl())) {
            this.customizeConfigurationFromUrl(builder);
        }
        this.customize(builder);
        return builder.build();
    }

    private void customize(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        this.builderCustomizers.orderedStream().forEach((customizer) -> {
            customizer.customize(builder);
        });
    }

    private void customizeConfigurationFromUrl(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = this.parseUrl(this.redisProperties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    private void applyPooling(RedisProperties.Pool pool, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(PoolConfigUtil.poolConfig(pool));
    }

    private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        if (this.redisProperties.getTimeout() != null) {
            Duration timeout = this.redisProperties.getTimeout();
            builder.readTimeout(timeout);
        }
        return builder;
    }
}
