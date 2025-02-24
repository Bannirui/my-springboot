package com.github.bannirui.msb.web.session;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * {@link RedisSessionStorageImpl}实现方式在redis上 就涉及redis的连接信息
 */
@ConfigurationProperties(
    prefix = "session.redis"
)
public class SessionRedisProperties {
    public static final String PREFIX = "session.redis";
    public static final String SESSION_TIMEOUT = "session.redis.session-timeout";
    private int database = 0;
    private String host;
    private String password;
    private int port = 6379;
    private boolean ssl;
    private Duration sessionTimeout;
    private SessionRedisProperties.Sentinel sentinel;
    private SessionRedisProperties.Cluster cluster;
    private String namespace;
    private Duration lettuceCommandTimeout;
    private Duration lettuceShutdownTimeout;
    private int lettuceIoThreadPoolSize = 3;
    private int lettuceComputationThreadPoolSize = 3;
    private boolean lettuceAutoReconnect = true;

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Duration getSessionTimeout() {
        return this.sessionTimeout;
    }

    public void setSessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public SessionRedisProperties.Sentinel getSentinel() {
        return this.sentinel;
    }

    public void setSentinel(SessionRedisProperties.Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public SessionRedisProperties.Cluster getCluster() {
        return this.cluster;
    }

    public void setCluster(SessionRedisProperties.Cluster cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Duration getLettuceShutdownTimeout() {
        return this.lettuceShutdownTimeout;
    }

    public void setLettuceShutdownTimeout(Duration lettuceShutdownTimeout) {
        this.lettuceShutdownTimeout = lettuceShutdownTimeout;
    }

    public Duration getLettuceCommandTimeout() {
        return this.lettuceCommandTimeout;
    }

    public void setLettuceCommandTimeout(Duration lettuceCommandTimeout) {
        this.lettuceCommandTimeout = lettuceCommandTimeout;
    }

    public int getLettuceIoThreadPoolSize() {
        return this.lettuceIoThreadPoolSize;
    }

    public void setLettuceIoThreadPoolSize(int lettuceIoThreadPoolSize) {
        this.lettuceIoThreadPoolSize = lettuceIoThreadPoolSize;
    }

    public int getLettuceComputationThreadPoolSize() {
        return this.lettuceComputationThreadPoolSize;
    }

    public void setLettuceComputationThreadPoolSize(int lettuceComputationThreadPoolSize) {
        this.lettuceComputationThreadPoolSize = lettuceComputationThreadPoolSize;
    }

    public boolean isLettuceAutoReconnect() {
        return this.lettuceAutoReconnect;
    }

    public void setLettuceAutoReconnect(boolean lettuceAutoReconnect) {
        this.lettuceAutoReconnect = lettuceAutoReconnect;
    }

    public static class Sentinel {
        private String master;
        private List<String> nodes;

        public String getMaster() {
            return this.master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }
    }

    public static class Cluster {
        private List<String> nodes;
        private Integer maxRedirects;

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }
    }
}
