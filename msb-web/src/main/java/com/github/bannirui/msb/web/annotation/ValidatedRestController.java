package com.github.bannirui.msb.web.annotation;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@Validated
public @interface ValidatedRestController {
}
