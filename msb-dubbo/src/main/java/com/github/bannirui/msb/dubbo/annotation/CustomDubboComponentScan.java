package com.github.bannirui.msb.dubbo.annotation;

import com.github.bannirui.msb.dubbo.autoconfig.DubboScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * dubbo扫描路径
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DubboScanRegistrar.class})
public @interface CustomDubboComponentScan {
    // 显性要扫描的包
    String[] value() default {};

    // 要扫描的包base路径
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
