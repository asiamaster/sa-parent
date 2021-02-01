package com.sa.oplog.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


@Inherited
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogParam {


    @AliasFor("value")
    String bindName() default "";


    boolean required() default false;


    @AliasFor("bindName")
    String value() default "";
}
