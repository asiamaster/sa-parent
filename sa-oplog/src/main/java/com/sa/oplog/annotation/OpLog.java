package com.sa.oplog.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


@Inherited
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    @AliasFor("value")
    String template() default "";


    @AliasFor("template")
    String value() default "";


    String handler() default "";


    String initializer() default "";


    String params() default "";


    String contentProvider() default "";



    String requiredExpr() default "";
}
