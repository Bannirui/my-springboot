package com.github.bannirui.msb.cache.redis.autoconfig;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.redis.DynamicJedisConnectionConfiguration;
import com.github.bannirui.msb.cache.redis.DynamicLettuceConnectionConfiguration;
import com.github.bannirui.msb.cache.redis.listener.RedisConfigurationRefresherListener;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;

@Configuration
@ConditionalOnClass({RedisOperations.class})
@AutoConfigureBefore({RedisAutoConfiguration.class})
@EnableConfigurationProperties({RedisProperties.class, CompositeCacheProperties.class})
@Import({DynamicLettuceConnectionConfiguration.class, DynamicJedisConnectionConfiguration.class})
public class DynamicRedisAutoConfiguration {

    @Bean
    public RedisConfigurationRefresherListener redisConfigurationRefresherListener() {
        return new RedisConfigurationRefresherListener();
    }
}
