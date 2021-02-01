package com.sa.metadata.handler;

import org.springframework.stereotype.Component;

import java.util.function.Function;


@Component
public class DefaultMismatchHandler implements Function {
    @Override
    public Object apply(Object o) {
        return o;
    }
}
