package com.github.com.bannirui.msb.sso.annotation;

import com.github.com.bannirui.msb.sso.autoconfig.MySsoImportSelectorController;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 启动sso场景.
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({MySsoImportSelectorController.class})
public @interface EnableMySso {

}
