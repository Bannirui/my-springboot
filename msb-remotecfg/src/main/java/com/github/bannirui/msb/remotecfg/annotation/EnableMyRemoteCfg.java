package com.github.bannirui.msb.remotecfg.annotation;

import com.github.bannirui.msb.remotecfg.autoconfig.MyRemoteCfgImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 注解启动远程配置中心场景.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({MyRemoteCfgImportSelector.class})
public @interface EnableMyRemoteCfg {

    /**
     * nacos的dataId.
     */
    String[] dataId() default {"application"};
}
