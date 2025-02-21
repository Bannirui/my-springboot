package com.github.bannirui.msb.http.annotation;

import com.github.bannirui.msb.http.autoconfig.HttpImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 注解启动Http场景.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({HttpImportSelector.class})
public @interface EnableHttp {
}
