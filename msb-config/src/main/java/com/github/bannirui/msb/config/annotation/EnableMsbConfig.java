package com.github.bannirui.msb.config.annotation;

import com.github.bannirui.msb.config.ConfigRegistrar;
import com.github.bannirui.msb.config.EnableMsbConfigChangeListenerSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 远程配置中心启动器.
 * <ul>
 *     <li>远程中心的配置加载到内存</li>
 *     <li>支持Spring的{@link org.springframework.beans.factory.annotation.Value}解析</li>
 *     <li>注册监听器到Apollo配置中心 支持热加载</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ConfigRegistrar.class, EnableMsbConfigChangeListenerSelector.class})
public @interface EnableMsbConfig {

    /**
     * 应用指定的namespace apollo为每个应用都生成默认的application 文件后缀是.properties 如果要换成其他格式文件就要带上后缀
     */
    String[] value() default {"application"};

    int order() default Ordered.LOWEST_PRECEDENCE;
}
