package com.github.bannirui.msb.common.constant;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * Spring的生命周期事件发布的监听器顺序 涉及到资源依赖.
 */
public interface AppEventListenerSort {

    /**
     * SpringBoot的log至少在{@link org.springframework.boot.context.event.ApplicationPreparedEvent}之后才能使用
     * 因为需要在{@link AbstractApplicationContext#refresh()}之后才能有{@link org.slf4j.Logger}的实例
     * 因此在此之前的日志想要输出就得自己手动注入一个实例
     */
    int MSB_BASE_LOG = -2147483628;

    // 需要使用日志进行输出了
    int MSB_BANNER = MSB_BASE_LOG + 1;

    // 接入Apollo
    int APOLLO_CONFIG = MSB_BANNER + 1;

    // Spring Env需要把Apollo的配置刷入内存
    int MSB_APP_SPRING_ENVIRONMENT_CONFIG = APOLLO_CONFIG + 1;

    // 接入cat日志
    int CAT_LOG = APOLLO_CONFIG + 1;
}
