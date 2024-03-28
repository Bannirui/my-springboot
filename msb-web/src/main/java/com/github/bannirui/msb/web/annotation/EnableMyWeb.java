package com.github.bannirui.msb.web.annotation;

import com.github.bannirui.msb.web.autoconfig.MyWebImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 注解启动Web场景.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({MyWebImportSelector.class})
public @interface EnableMyWeb {
}
