package com.github.bannirui.msb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在classpath:/META-INF/msb/plugin下配置拦截器 注解指定同一扩展点拦截器的优先级
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MsbPlugin {
    /**
     * @return 值越大优先级越低
     */
    int order() default 0;
}
