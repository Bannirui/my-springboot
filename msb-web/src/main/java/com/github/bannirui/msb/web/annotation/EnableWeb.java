package com.github.bannirui.msb.web.annotation;

import com.github.bannirui.msb.web.autoconfig.WebImportSelectorController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 注解启动Web场景.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({WebImportSelectorController.class})
public @interface EnableWeb {
    /**
     * session有保存有2种方式
     * <ul>
     *     <li>缓存在内存 实现方式是{@link com.github.bannirui.msb.web.session.MapSessionStorageImpl}</li>
     *     <li>持久化在redis 实现方式是{@link com.github.bannirui.msb.web.session.RedisSessionStorageImpl}</li>
     * </ul>
     */
    String sessionType() default "com.github.bannirui.msb.web.session.MapSessionStorageImpl";

    String sessionStrategy() default "COOKIE";
}
