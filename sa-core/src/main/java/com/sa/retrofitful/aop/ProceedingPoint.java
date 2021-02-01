package com.sa.retrofitful.aop;

import java.lang.reflect.Method;


public interface ProceedingPoint {

    Method getMethod();

    void setMethod(Method method);

    Object[] getArgs();

    void setArgs(Object[] args);

    Object proceed() throws Throwable;

    Object proceed(Object[] args) throws Throwable;
}
