package com.sa.domain.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlOperator {
    public static final String AND = "and";
    public static final String OR = "or";
    String value() default AND;
}
