package com.sa.retrofitful.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface VOBody {
    boolean required() default true;
}
