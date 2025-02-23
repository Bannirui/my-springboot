package com.github.bannirui.msb.cache.annotation;

import com.github.bannirui.msb.cache.CacheType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(CacheConfigOptionsContainer.class)
public @interface CacheConfigOptions {
    String[] cacheNames();

    String spec() default "";

    int initialCapacity() default 0;

    long maximumSize() default 1024L;

    int concurrencyLevel() default 0;

    String expireAfterAccess() default "";

    String expireAfterWrite() default "";

    long expired() default 300L;

    CacheType cacheType() default CacheType.ALL;

    boolean cacheNull() default true;
}
