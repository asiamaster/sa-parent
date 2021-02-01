package com.sa.retrofitful.aop;


public interface RestfulAspect {

    Object around(ProceedingPoint proceedingPoint);
}
