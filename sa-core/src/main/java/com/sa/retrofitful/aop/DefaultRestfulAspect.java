package com.sa.retrofitful.aop;

import org.springframework.stereotype.Component;

@Component
public class DefaultRestfulAspect implements RestfulAspect {

    @Override
    public Object around(ProceedingPoint proceedingPoint) {
        return null;
    }
}
