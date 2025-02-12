package com.github.bannirui.msb.dubbo.annotation;

import com.github.bannirui.msb.dubbo.autoconfig.ComponentImportRegistrar;
import com.github.bannirui.msb.dubbo.autoconfig.ComponentImportSelector;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ComponentImportSelector.class, ComponentImportRegistrar.class})
@EnableDubboConfig
@CustomDubboComponentScan
public @interface EnableMsbDubbo {
    // todo
    @Deprecated
    String port() default "20880";

    // todo
    @Deprecated
    String protocol() default "dubbo";

    // todo
    @Deprecated
    String scanPackageName() default "com.github.bannirui";

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
