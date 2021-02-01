package com.sa.dto;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({DTORegistrar.class})
public @interface DTOScan {

    String[] value() default {};

    String file() default "";
}
