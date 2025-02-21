package com.github.bannirui.msb.http.annotation;

import com.github.bannirui.msb.http.enums.ParamDataType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpParam {
    @AliasFor("paramName")
    String name();

    ParamDataType dataType() default ParamDataType.JSON;
}
