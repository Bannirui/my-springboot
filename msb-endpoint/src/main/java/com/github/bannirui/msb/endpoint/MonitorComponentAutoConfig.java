package com.github.bannirui.msb.endpoint;

import com.github.bannirui.msb.endpoint.condition.ConditionalOnConfig;
import com.github.bannirui.msb.endpoint.jmx.ConditionalOnActivatedMonitor;
import com.github.bannirui.msb.endpoint.jmx.DubboThreadMonitor;
import com.github.bannirui.msb.endpoint.jmx.JedisDataMonitor;
import com.github.bannirui.msb.endpoint.jmx.JvmDataMonitor;
import com.github.bannirui.msb.endpoint.jmx.LettuceDataMonitor;
import com.github.bannirui.msb.endpoint.jmx.TomcatDataMonitor;
import io.lettuce.core.RedisClient;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import redis.clients.jedis.Jedis;

@Configuration
@AutoConfigureAfter({HealthIndicatorAutoConfig.class, RedisAutoConfiguration.class})
public class MonitorComponentAutoConfig {

    @Bean
    @ConditionalOnActivatedMonitor(
        name = "jvm"
    )
    @ConditionalOnMissingBean(
        name = {"jvmDataMonitor"}
    )
    public JvmDataMonitor jvmDataMonitor() {
        return new JvmDataMonitor();
    }

    @ConditionalOnConfig(
        prefix = "spring.redis",
        entity = RedisProperties.class
    )
    @ConditionalOnActivatedMonitor(
        name = "lettuce"
    )
    @ConditionalOnClass({RedisClient.class, LettuceConnection.class, RedisConnectionFactory.class})
    @ConditionalOnBean({RedisConnectionFactory.class})
    @ConditionalOnMissingBean(
        name = {"lettuceDataMonitor"}
    )
    static class LettuceDataMonitorAutoConfig {
        @Bean
        public LettuceDataMonitor lettuceDataMonitor() {
            return new LettuceDataMonitor();
        }
    }

    @ConditionalOnConfig(
        prefix = "spring.redis",
        entity = RedisProperties.class
    )
    @ConditionalOnActivatedMonitor(
        name = "jedis"
    )
    @ConditionalOnClass({GenericObjectPool.class, Jedis.class, JedisConnection.class, RedisConnectionFactory.class})
    @ConditionalOnBean({RedisConnectionFactory.class})
    @ConditionalOnMissingBean(
        name = {"jedisDataMonitor"}
    )
    static class JedisDataMonitorAutoConfig {
        @Bean
        public JedisDataMonitor jedisDataMonitor() {
            return new JedisDataMonitor();
        }
    }

    @ConditionalOnActivatedMonitor(
        name = "dubbo"
    )
    @ConditionalOnBean({EnableDubbo.class})
    @ConditionalOnMissingBean(
        name = {"dubboThreadMonitor"}
    )
    static class DubboThreadMonitorAutoConfig {
        @Bean
        public DubboThreadMonitor dubboThreadMonitor() {
            return new DubboThreadMonitor();
        }
    }

    @ConditionalOnActivatedMonitor(
        name = "tomcat"
    )
    @ConditionalOnClass({Tomcat.class})
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(
        name = {"tomcatDataMonitor"}
    )
    static class TomcatDataMonitorAutoConfig {
        @Bean
        public TomcatDataMonitor tomcatDataMonitor() {
            return new TomcatDataMonitor();
        }
    }
}
