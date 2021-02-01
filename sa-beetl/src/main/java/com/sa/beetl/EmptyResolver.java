package com.sa.beetl;

import org.springframework.stereotype.Component;


@Component
public class EmptyResolver implements VirtualAttributeResolver {
    @Override
    public String resolve(Object o, String attrName) {
        return "";
    }

    @Override
    public Class resolveClass() {
        return this.getClass();
    }
}