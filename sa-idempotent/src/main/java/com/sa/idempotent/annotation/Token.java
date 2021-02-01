package com.sa.idempotent.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {

    @AliasFor("value")
    String url() default "";

    @AliasFor("url")
    String value() default "";

}
