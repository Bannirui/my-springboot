package com.github.bannirui.msb.web.annotation;

import com.github.bannirui.msb.web.autoconfig.WebImportSelectorController;
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
@Import({WebImportSelectorController.class})
public @interface EnableWeb {
    String sessionType() default "com.github.bannirui.msb.web.session.MapSessionStorageImpl";

    String sessionStrategy() default "COOKIE";
}
