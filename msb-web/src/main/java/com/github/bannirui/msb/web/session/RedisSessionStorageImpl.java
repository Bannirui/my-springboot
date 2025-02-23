package com.github.bannirui.msb.web.session;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RedisSessionStorageImpl implements ISessionStorage {
    private static Logger log = LoggerFactory.getLogger(RedisSessionStorageImpl.class);
    private final String namespace;
    private HashOperations<String, String, Object> hashOperations;
    private SetOperations<String, String> openIdSetOperations;
    private RedisSerializer<Object> defaultSerializer = new JdkSerializationRedisSerializer();
    private ClientResources clientResources;

    public RedisSessionStorageImpl(ConfigurableEnvironment env) {
        TitansSessionRedisProperties sessionRedisProperties =
            Binder.get(env).bind("titans.session.redis", TitansSessionRedisProperties.class).orElse(new TitansSessionRedisProperties());
        this.namespace = sessionRedisProperties.getNamespace() != null ? sessionRedisProperties.getNamespace() + "_v4" : MsbEnvironmentMgr.getAppName() + "_v4";
        SessionLettuceClusterProperties lettuceClusterProperties = (SessionLettuceClusterProperties)Binder.get(env).bind("session.redis.lettuce.cluster", SessionLettuceClusterProperties.class).orElse(new SessionLettuceClusterProperties());
        TitansSessionRedisProperties.Cluster cluster = sessionRedisProperties.getCluster();
        Object clientOptions;
        if (cluster != null) {
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            builder.autoReconnect(sessionRedisProperties.isLettuceAutoReconnect());
            if (cluster.getMaxRedirects() != null) {
                builder.maxRedirects(cluster.getMaxRedirects());
            }
            ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder();
            if (lettuceClusterProperties.getRefresh().getPeriod() != null && !lettuceClusterProperties.getRefresh().getPeriod().isNegative()) {
                refreshBuilder.enablePeriodicRefresh(lettuceClusterProperties.getRefresh().getPeriod());
            }
            if (lettuceClusterProperties.getRefresh().isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            builder.timeoutOptions(TimeoutOptions.enabled(lettuceClusterProperties.getCommandTimeout()));
            builder.topologyRefreshOptions(refreshBuilder.build());
            clientOptions = builder.build();
        } else {
            io.lettuce.core.ClientOptions.Builder builder = ClientOptions.builder();
            builder.autoReconnect(sessionRedisProperties.isLettuceAutoReconnect());
            clientOptions = builder.build();
        }
        this.clientResources = ClientResources.builder().ioThreadPoolSize(sessionRedisProperties.getLettuceIoThreadPoolSize()).computationThreadPoolSize(sessionRedisProperties.getLettuceComputationThreadPoolSize()).build();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder = LettuceClientConfiguration.builder().clientName("redis-httpSession").clientOptions((ClientOptions)clientOptions).clientResources(this.clientResources);
        if (sessionRedisProperties.getLettuceShutdownTimeout() != null) {
            lettuceClientConfigurationBuilder.shutdownTimeout(sessionRedisProperties.getLettuceShutdownTimeout());
        }
        if (sessionRedisProperties.getLettuceCommandTimeout() != null) {
            lettuceClientConfigurationBuilder.commandTimeout(sessionRedisProperties.getLettuceCommandTimeout());
        }
        LettuceClientConfiguration lettuceConfig;
        if (sessionRedisProperties.isSsl()) {
            lettuceConfig = lettuceClientConfigurationBuilder.useSsl().build();
        } else {
            lettuceConfig = lettuceClientConfigurationBuilder.build();
        }
        LettuceConnectionFactory lettuceFactory = this.newLettuceConnectionFactory(sessionRedisProperties, lettuceConfig);
        lettuceFactory.afterPropertiesSet();
        StringRedisTemplate strTemplate = new StringRedisTemplate(lettuceFactory);
        strTemplate.setHashValueSerializer(this.defaultSerializer);
        strTemplate.setHashKeySerializer(this.defaultSerializer);
        this.hashOperations = strTemplate.opsForHash();
        this.openIdSetOperations = strTemplate.opsForSet();
    }

    private LettuceConnectionFactory newLettuceConnectionFactory(TitansSessionRedisProperties sessionRedisProperties, LettuceClientConfiguration lettuceConfig) {
        RedisStandaloneConfiguration standaloneConfig = this.getStandaloneConfig(sessionRedisProperties);
        if (standaloneConfig != null) {
            return new LettuceConnectionFactory(standaloneConfig, lettuceConfig);
        } else {
            RedisSentinelConfiguration sentinelConfig = this.getSentinelConfig(sessionRedisProperties);
            if (sentinelConfig != null) {
                return new LettuceConnectionFactory(sentinelConfig, lettuceConfig);
            } else {
                RedisClusterConfiguration clusterConfig = this.getClusterConfiguration(sessionRedisProperties);
                return clusterConfig != null ? new LettuceConnectionFactory(clusterConfig, lettuceConfig) : null;
            }
        }
    }

    @Override
    public void remove(String id) {
        this.hashOperations.getOperations().delete(this.getSessionId(id));
        String openId = this.getOpenId(id);
        if (openId != null) {
            this.openIdSetOperations.remove(openId, new Object[]{this.getSessionId(id)});
        }
    }

    @Override
    public Map<String, Object> get(String id) {
        try {
            return this.hashOperations.entries(this.getSessionId(id));
        } catch (Exception e) {
            if (e.getClass().isAssignableFrom(SerializationException.class)) {
                return null;
            } else {
                log.error("Titans[SSO]: get session info from redis throws exception!", e);
                return null;
            }
        }
    }

    @Override
    public void save(String id, Map<String, Object> sessionAttrs) {
        this.hashOperations.putAll(this.getSessionId(id), sessionAttrs);
        String openId = this.getOpenId(id);
        if (openId != null) {
            this.openIdSetOperations.add(openId, id);
        }
    }

    @Override
    public void ttl(String id, Integer maxInactiveInterval) {
        this.hashOperations.getOperations().expire(this.getSessionId(id), maxInactiveInterval.longValue(), TimeUnit.SECONDS);
        String openId = this.getOpenId(id);
        if (openId != null) {
            this.openIdSetOperations.getOperations().expire(openId, maxInactiveInterval.longValue(), TimeUnit.SECONDS);
        }
    }

    private String getOpenId(String sessionId) {
        Map<String, Object> map = this.hashOperations.entries(sessionId);
        if (map != null) {
            Iterator var3 = map.entrySet().iterator();
            while(var3.hasNext()) {
                Entry<String, Object> entry = (Entry)var3.next();
                String k = (String)entry.getKey();
                Object v = entry.getValue();
                if ("SPRING_SECURITY_CONTEXT".equals(k) && v instanceof SecurityContext && ((SecurityContext)v).getAuthentication() instanceof User) {
                    User user = (User)((SecurityContext)v).getAuthentication();
                    String openId = (String)user.getProperties("openid");
                    return this.formatOpenId(openId);
                }
            }
        }
        return null;
    }

    @Override
    public void put(String id, String key, Object value) {
        this.hashOperations.put(this.getSessionId(id), key, value);
        if ("SPRING_SECURITY_CONTEXT".equals(key) && value instanceof SecurityContext && ((SecurityContext)value).getAuthentication() instanceof User) {
            Use user = (User)((SecurityContext)value).getAuthentication();
            String openid = this.formatOpenId((String)user.getProperties("openid"));
            if (openid != null) {
                this.openIdSetOperations.add(openid, new String[]{id});
            }
        }

    }

    public List<User> getUser(String openId) {
        Set<String> sessionIdSet = this.openIdSetOperations.members(this.formatOpenId(openId));
        if (sessionIdSet != null && sessionIdSet.size() != 0) {
            List<User> users = new ArrayList();
            Iterator var4 = sessionIdSet.iterator();

            while(true) {
                Map map;
                do {
                    if (!var4.hasNext()) {
                        return users;
                    }

                    String sessionId = (String)var4.next();
                    map = this.hashOperations.entries(this.getSessionId(sessionId));
                } while(map == null);

                Iterator var7 = map.entrySet().iterator();

                while(var7.hasNext()) {
                    Entry<String, Object> entry = (Entry)var7.next();
                    String k = (String)entry.getKey();
                    Object v = entry.getValue();
                    if ("SPRING_SECURITY_CONTEXT".equals(k) && ((SecurityContext)v).getAuthentication() instanceof User) {
                        User user = (User)((SecurityContext)v).getAuthentication();
                        users.add(user);
                    }
                }
            }
        } else {
            return null;
        }
    }

    public void updateUser(List<User> users) {
        if (users != null && users.size() != 0) {
            Iterator var2 = users.iterator();

            User user;
            Set sessionIdSet;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                user = (User)var2.next();
                String openid = user.getOpenid();
                sessionIdSet = this.openIdSetOperations.members(this.formatOpenId(openid));
            } while(sessionIdSet == null || sessionIdSet.size() == 0);

            Iterator var6 = sessionIdSet.iterator();

            while(var6.hasNext()) {
                String sessionId = (String)var6.next();
                SecurityContextHolder.getContext().setAuthentication(user);
                this.hashOperations.put(this.getSessionId(sessionId), "SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            }

        }
    }

    private String formatOpenId(String openId) {
        return this.namespace + ":" + openId;
    }

    public void remove(String id, String attributeName) {
        this.hashOperations.delete(this.getSessionId(id), new Object[]{attributeName});
    }

    public void destroy() {
        this.clientResources.shutdown();
    }

    private String getSessionId(String id) {
        return this.namespace + ":" + id;
    }

    private RedisStandaloneConfiguration getStandaloneConfig(TitansSessionRedisProperties redisProperties) {
        if (StringUtils.hasText(redisProperties.getHost())) {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisProperties.getHost());
            config.setPort(redisProperties.getPort());
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
            config.setDatabase(redisProperties.getDatabase());
            return config;
        } else {
            return null;
        }
    }

    private RedisSentinelConfiguration getSentinelConfig(TitansSessionRedisProperties redisProperties) {
        if (redisProperties.getSentinel() == null) {
            return null;
        } else {
            Sentinel sentinelProperties = redisProperties.getSentinel();
            Set<String> sentinelHostAndPorts = new HashSet(sentinelProperties.getNodes());
            RedisSentinelConfiguration config = new RedisSentinelConfiguration(sentinelProperties.getMaster(), sentinelHostAndPorts);
            if (redisProperties.getPassword() != null) {
                config.setPassword(RedisPassword.of(redisProperties.getPassword()));
            }

            config.setDatabase(redisProperties.getDatabase());
            return config;
        }
    }

    private RedisClusterConfiguration getClusterConfiguration(TitansSessionRedisProperties redisProperties) {
        if (redisProperties.getCluster() == null) {
            return null;
        } else {
            Cluster clusterProperties = redisProperties.getCluster();
            RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
            if (clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }

            if (redisProperties.getPassword() != null) {
                config.setPassword(RedisPassword.of(redisProperties.getPassword()));
            }

            return config;
        }
    }
}
