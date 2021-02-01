package com.sa.idempotent.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    public static final String HEADER = "header";

    public static final String PARAMETER = "parameter";


    @AliasFor("type")
    String value() default "header";

    @AliasFor("value")
    String type() default "header";
}
