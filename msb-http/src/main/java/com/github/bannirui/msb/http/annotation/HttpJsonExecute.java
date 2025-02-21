package com.github.bannirui.msb.http.annotation;

import com.github.bannirui.msb.http.constructor.DefaultHttpRequestBodyConstructor;
import com.github.bannirui.msb.http.enums.JsonRequestMethod;
import com.github.bannirui.msb.http.enums.ResponseType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpJsonExecute {
    @AliasFor("url")
    String value();

    @AliasFor("method")
    JsonRequestMethod method() default JsonRequestMethod.POST;

    @AliasFor("type")
    ResponseType type() default ResponseType.JSON;

    int connectTime() default 60000;

    int socketConnectTime() default 60000;

    Class<?> constructor() default DefaultHttpRequestBodyConstructor.class;

    String[] extraParam() default {};

    int connectionRequestTimeout() default 60000;
}
