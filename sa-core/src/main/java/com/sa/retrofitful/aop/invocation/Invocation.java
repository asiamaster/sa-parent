package com.sa.retrofitful.aop.invocation;

import java.lang.reflect.Method;


public class Invocation {

    private Method method;

    private Object[] args;

    private Object proxy;


    private Class<?> proxyClazz;

    public Class<?> getProxyClazz() {
        return proxyClazz;
    }

    public void setProxyClazz(Class<?> proxyClazz) {
        this.proxyClazz = proxyClazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

}
