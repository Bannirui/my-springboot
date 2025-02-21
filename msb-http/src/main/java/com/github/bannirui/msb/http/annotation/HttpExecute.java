package com.github.bannirui.msb.http.annotation;

import com.github.bannirui.msb.http.enums.ResponseType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMethod;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpExecute {
    @AliasFor("url")
    String value();

    @AliasFor("method")
    RequestMethod method() default RequestMethod.POST;

    @AliasFor("type")
    ResponseType type() default ResponseType.JSON;

    int connectTime() default 60000;

    int socketConnectTime() default 60000;

    int connectionRequestTimeout() default 60000;
}
