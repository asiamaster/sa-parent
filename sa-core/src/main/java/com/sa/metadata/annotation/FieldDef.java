package com.sa.metadata.annotation;

import java.lang.annotation.*;
import java.util.function.Function;


@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDef {


	String label() default "";


	int maxLength() default -1;


	String defValue() default "";


	Class<? extends Function> handler() default Function.class;

}
