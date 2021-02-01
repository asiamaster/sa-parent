package com.sa.retrofitful.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    String value() default "";
}
