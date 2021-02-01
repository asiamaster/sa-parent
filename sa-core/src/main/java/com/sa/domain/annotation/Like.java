package com.sa.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;


@Documented
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Like {
    public static final String LEFT="LEFT";
    public static final String RIGHT="RIGHT";
    public static final String BOTH="BOTH";
    String value() default BOTH;
}
