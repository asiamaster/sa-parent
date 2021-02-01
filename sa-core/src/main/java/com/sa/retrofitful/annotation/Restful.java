package com.sa.retrofitful.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Restful {
    @AliasFor("value")
    String baseUrl() default "";

    @AliasFor("baseUrl")
    String value() default "";
}
