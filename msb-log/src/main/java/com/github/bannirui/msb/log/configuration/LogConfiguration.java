package com.github.bannirui.msb.log.configuration;

import com.github.bannirui.msb.log.autoconfigure.EnableMsbLogImportSelector;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * <ul>
 *     <li>{@link com.github.bannirui.msb.log.annotation.EnableMsbLog}中通过{@link org.springframework.context.annotation.Import}注解向容器中注入了{@link EnableMsbLogImportSelector}</li>
 *     <li>最终向容器中注入了当前类</li>
 *     <li>当前类又注入了{@link DynamicLogSystem}</li>
 * </ul>
 * 也就是说在容器执行完{@link AbstractApplicationContext#refresh()}之后容器中会被注入{@link DynamicLogSystem}的实例
 * 则{@link DynamicLogSystem}的使用时机在{@link org.springframework.boot.context.event.ApplicationPreparedEvent}时机
 * <ul>
 *     <li>应用成功启动 {@link org.springframework.boot.context.event.ApplicationStartedEvent}</li>
 *     <li>应用已经准备好 {@link org.springframework.boot.context.event.ApplicationReadyEvent}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "logging")
public class LogConfiguration {

    @Bean
    public DynamicLogSystem dynamicLogSystem(LoggingSystem loggingSystem) {
        return new DynamicLogSystem(loggingSystem);
    }
}
