package com.github.bannirui.msb.dubbo.annotation;

import com.github.bannirui.msb.dubbo.autoconfig.ComponentImportRegistrar;
import com.github.bannirui.msb.dubbo.autoconfig.ComponentImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ComponentImportSelector.class, ComponentImportRegistrar.class})
@EnableDubboConfig
@CustomDubboComponentScan
public @interface EnableDubbo {
    String port() default "20880";

    String protocol() default "dubbo";

    String scanPackageName() default "com.zto";

    @AliasFor(
        annotation = CustomDubboComponentScan.class,
        attribute = "basePackages"
    )
    String[] scanBasePackages() default {};

    @AliasFor(
        annotation = CustomDubboComponentScan.class,
        attribute = "basePackageClasses"
    )
    Class<?>[] scanBasePackageClasses() default {};

    @AliasFor(
        annotation = EnableDubboConfig.class,
        attribute = "multiple"
    )
    boolean multipleConfig() default true;
}
