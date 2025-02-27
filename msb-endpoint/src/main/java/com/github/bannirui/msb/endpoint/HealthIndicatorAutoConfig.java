package com.github.bannirui.msb.endpoint;

import com.github.bannirui.msb.endpoint.condition.ConditionalOnConfig;
import com.github.bannirui.msb.endpoint.health.ConditionalOnActivatedHealthIndicator;
import com.github.bannirui.msb.endpoint.health.CpuHealthlndicator;
import com.github.bannirui.msb.endpoint.health.DiskSpaceHealthIndicator;
import com.github.bannirui.msb.endpoint.health.DubboHealthIndicator;
import com.github.bannirui.msb.endpoint.health.MQHealthIndicator;
import com.github.bannirui.msb.endpoint.health.MemoryHealthIndicator;
import com.github.bannirui.msb.endpoint.health.RedisHealthIndicator;
import com.github.bannirui.msb.mq.annotation.EnableMsbMQ;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@AutoConfigureAfter({RedisAutoConfiguration.class})
@AutoConfigureOrder(HealthIndicatorAutoConfig.ORDER)
public class HealthIndicatorAutoConfig {
    public static final int ORDER = 500;

    @Bean
    @ConditionalOnActivatedHealthIndicator(
        name = "diskSpace"
    )
    @ConditionalOnMissingBean(
        name = {"diskSpaceHealthIndicator"}
    )
    public DiskSpaceHealthIndicator diskSpaceHealthIndicator() {
        return new DiskSpaceHealthIndicator();
    }

    @Bean
    @ConditionalOnActivatedHealthIndicator(
        name = "memory"
    )
    @ConditionalOnMissingBean(
        name = {"memoryHealthIndicator"}
    )
    public MemoryHealthIndicator memoryHealthIndicator() {
        return new MemoryHealthIndicator();
    }

    @Bean
    @ConditionalOnActivatedHealthIndicator(
        name = "cpu"
    )
    @ConditionalOnMissingBean(
        name = {"cpuHealthIndicator"}
    )
    public CpuHealthlndicator cpuHealthIndicator() {
        return new CpuHealthlndicator();
    }

    @ConditionalOnActivatedHealthIndicator(
        name = "dubbo"
    )
    @ConditionalOnBean({EnableDubbo.class})
    @ConditionalOnMissingBean(
        name = {"dubboHealthIndicator"}
    )
    static class DubboHealthIndicatorAutoConfig {
        @Bean
        public DubboHealthIndicator dubboHealthIndicator() {
            return new DubboHealthIndicator();
        }
    }

    @ConditionalOnConfig(
        prefix = "spring.redis",
        entity = RedisProperties.class
    )
    @ConditionalOnActivatedHealthIndicator(
        name = "redis"
    )
    @ConditionalOnBean({RedisConnectionFactory.class})
    @ConditionalOnMissingBean(
        name = {"redisHealthIndicator"}
    )
    static class RedisHealthIndicatorAutoConfig {
        @Bean
        public RedisHealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
            return new RedisHealthIndicator(redisConnectionFactory);
        }
    }

    @ConditionalOnActivatedHealthIndicator(
        name = "mq",
        enabled = false
    )
    @ConditionalOnClass({EnableMsbMQ.class})
    static class MQHealthIndicatorAutoConfig {
        @ConditionalOnMissingBean(
            name = {"mqHealthIndicator"}
        )
        @Bean
        public MQHealthIndicator mqHealthIndicator() {
            return new MQHealthIndicator();
        }
    }
}
