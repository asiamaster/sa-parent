package com.sa.retrofitful.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {
    String value() default "";
}
