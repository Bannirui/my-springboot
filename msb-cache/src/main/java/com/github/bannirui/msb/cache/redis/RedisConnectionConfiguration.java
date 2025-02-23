package com.github.bannirui.msb.cache.redis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;

public abstract class RedisConnectionConfiguration {
    private final RedisSentinelConfiguration sentinelConfiguration;
    private final RedisClusterConfiguration clusterConfiguration;

    public RedisConnectionConfiguration(ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider, ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        this.sentinelConfiguration = sentinelConfigurationProvider.getIfAvailable();
        this.clusterConfiguration = clusterConfigurationProvider.getIfAvailable();
    }

    protected final RedisSentinelConfiguration getSentinelConfig(RedisProperties redisProperties) {
        if (this.sentinelConfiguration != null) {
            return this.sentinelConfiguration;
        }
        RedisProperties.Sentinel sentinelProperties = redisProperties.getSentinel();
        if (Objects.isNull(sentinelProperties)) return null;
        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.master(sentinelProperties.getMaster());
        config.setSentinels(this.createSentinels(sentinelProperties));
        if (redisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        config.setDatabase(redisProperties.getDatabase());
        return config;
    }

    private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for(String node: sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            } catch (RuntimeException e) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", e);
            }
        }
        return nodes;
    }

    protected final RedisClusterConfiguration getClusterConfiguration(RedisProperties redisProperties) {
        if (this.clusterConfiguration != null) {
            return this.clusterConfiguration;
        }
        RedisProperties.Cluster clusterProperties = redisProperties.getCluster();
        if (clusterProperties == null) {
            return null;
        }
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        if (redisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        return config;
    }

    protected final RedisStandaloneConfiguration getStandaloneConfig(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.isNotBlank(redisProperties.getUrl())) {
            RedisConnectionConfiguration.ConnectionInfo connectionInfo = this.parseUrl(redisProperties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(redisProperties.getHost());
            config.setPort(redisProperties.getPort());
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        config.setDatabase(redisProperties.getDatabase());
        return config;
    }

    protected RedisConnectionConfiguration.ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            boolean useSsl = url.startsWith("redis://");
            String password = null;
            if (uri.getUserInfo() != null) {
                password = uri.getUserInfo();
                int index = password.indexOf(58);
                if (index >= 0) {
                    password = password.substring(index + 1);
                }
            }
            return new RedisConnectionConfiguration.ConnectionInfo(uri, useSsl, password);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed url '" + url + "'", e);
        }
    }

    protected static class ConnectionInfo {
        private final URI uri;
        private final boolean useSsl;
        private final String password;

        public ConnectionInfo(URI uri, boolean useSsl, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.password = password;
        }

        public boolean isUseSsl() {
            return this.useSsl;
        }

        public String getHostName() {
            return this.uri.getHost();
        }

        public int getPort() {
            return this.uri.getPort();
        }

        public String getPassword() {
            return this.password;
        }
    }
}
