package com.sa.retrofitful.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqParam {
    String value();
    boolean required() default true;
}
