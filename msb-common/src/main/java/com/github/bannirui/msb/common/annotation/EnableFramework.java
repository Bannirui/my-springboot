package com.github.bannirui.msb.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.core.annotation.AliasFor;

/**
 * 启用SpringBoot.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, TransactionAutoConfiguration.class}
)
public @interface EnableFramework {

    /**
     * SpringBoot启动 扫包路径.
     */
    @AliasFor(
        annotation = SpringBootApplication.class,
        attribute = "scanBasePackages"
    )
    String[] scanBasePackages() default {};
}
