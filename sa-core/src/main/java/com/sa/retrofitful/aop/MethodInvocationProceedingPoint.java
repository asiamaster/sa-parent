package com.sa.retrofitful.aop;

import java.lang.reflect.Method;

public class MethodInvocationProceedingPoint implements ProceedingPoint {

    private Method method;

    private Object[] args;
    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public Object proceed() throws Throwable {
        return null;
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return null;
    }
}
