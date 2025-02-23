package com.github.bannirui.msb.cache.redis;

import com.github.bannirui.msb.cache.redis.connection.DynamicLettuceConnectionFactory;
import com.github.bannirui.msb.cache.redis.connection.LettuceClusterProperties;
import com.github.bannirui.msb.cache.util.PoolConfigUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

@Configuration
@ConditionalOnClass({RedisClient.class})
@ConditionalOnProperty(
    name = {"spring.redis.client-type"},
    havingValue = "lettuce",
    matchIfMissing = true
)
@EnableConfigurationProperties({LettuceClusterProperties.class})
public class DynamicLettuceConnectionConfiguration extends RedisConnectionConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLettuceConnectionConfiguration.class);
    private RedisProperties redisProperties;
    private LettuceClusterProperties lettuceClusterProperties;
    private ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers;
    private ObjectProvider<LettuceClusterClientOptionsBuilderCustomizer> clusterClientCustomizers;

    public DynamicLettuceConnectionConfiguration(RedisProperties redisProperties, LettuceClusterProperties lettuceClusterProperties, ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider, ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider, ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers, ObjectProvider<LettuceClusterClientOptionsBuilderCustomizer> clusterClientCustomizers) {
        super(sentinelConfigurationProvider, clusterConfigurationProvider);
        this.redisProperties = redisProperties;
        this.lettuceClusterProperties = lettuceClusterProperties;
        this.builderCustomizers = builderCustomizers;
        this.clusterClientCustomizers = clusterClientCustomizers;
    }

    @Bean(
        destroyMethod = "shutdown"
    )
    @ConditionalOnMissingBean({ClientResources.class})
    public DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    @Bean(
        name = {"dynamicRedisConnectionFactory"}
    )
    @ConditionalOnMissingBean({RedisConnectionFactory.class})
    public DynamicLettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        LettuceClientConfiguration clientConfiguration = this.getLettuceClientConfiguration(clientResources, this.redisProperties);
        DynamicLettuceConnectionFactory factory = new DynamicLettuceConnectionFactory(this.createLettuceConnectionFactory(clientConfiguration, this.redisProperties));
        logger.info("DynamicLettuceConnectionFactory {} has created.", factory);
        return factory;
    }

    public LettuceClientConfiguration getLettuceClientConfiguration(ClientResources clientResources, RedisProperties redisProperties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = this.createBuilder(redisProperties);
        this.applyProperties(builder, redisProperties);
        if (StringUtils.isNotBlank(redisProperties.getUrl())) {
            this.customizeConfigurationFromUrl(redisProperties, builder);
        }
        builder.clientResources(clientResources);
        this.customize(builder);
        return builder.build();
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties redisProperties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        if (redisProperties.getLettuce().getPool() == null) {
            builder = LettuceClientConfiguration.builder();
        } else {
            builder = (new DynamicLettuceConnectionConfiguration.PoolBuilderFactory()).createBuilder(redisProperties.getLettuce().getPool());
        }
        if (Objects.nonNull(redisProperties.getCluster())) {
            ClusterClientOptions.Builder clusterOptionsBuilder = ClusterClientOptions.builder();
            if (redisProperties.getCluster().getMaxRedirects() != null) {
                clusterOptionsBuilder.maxRedirects(redisProperties.getCluster().getMaxRedirects());
            }
            io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder();
            if (this.lettuceClusterProperties.getRefresh().getPeriod() != null && !this.lettuceClusterProperties.getRefresh().getPeriod().isNegative()) {
                refreshBuilder.enablePeriodicRefresh(this.lettuceClusterProperties.getRefresh().getPeriod());
            }
            if (this.lettuceClusterProperties.getRefresh().isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            clusterOptionsBuilder.timeoutOptions(TimeoutOptions.enabled(this.lettuceClusterProperties.getCommandTimeout()));
            this.clusterClientCustomizers.orderedStream().forEach((customizer) -> {
                customizer.customize(clusterOptionsBuilder, refreshBuilder);
            });
            clusterOptionsBuilder.topologyRefreshOptions(refreshBuilder.build());
            return builder.clientOptions(clusterOptionsBuilder.build());
        } else {
            return builder;
        }
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyProperties(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder, RedisProperties redisProperties) {
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
            }
        }
        return builder;
    }

    private void customizeConfigurationFromUrl(RedisProperties redisProperties, LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = this.parseUrl(redisProperties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    private void customize(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        this.builderCustomizers.orderedStream().forEach((customizer) -> {
            customizer.customize(builder);
        });
    }

    public LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration, RedisProperties redisProperties) {
        if (Objects.nonNull(redisProperties.getSentinel())) {
            return new LettuceConnectionFactory(this.getSentinelConfig(redisProperties), clientConfiguration);
        } else {
            return Objects.nonNull(redisProperties.getCluster()) ? new LettuceConnectionFactory(this.getClusterConfiguration(redisProperties), clientConfiguration) : new LettuceConnectionFactory(this.getStandaloneConfig(redisProperties), clientConfiguration);
        }
    }

    private static class PoolBuilderFactory {

        public LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool properties) {
            return LettucePoolingClientConfiguration.builder().poolConfig(this.getPoolConfig(properties));
        }

        private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties.Pool properties) {
            return PoolConfigUtil.poolConfig(properties);
        }
    }
}
