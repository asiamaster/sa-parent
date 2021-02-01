package com.sa.seata.annotation;

import java.lang.annotation.*;


@Inherited
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalTx {

}
