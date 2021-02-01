package com.sa.beetl;


public interface VirtualAttributeResolver {

    public String resolve(Object o, String attrName);


    public Class resolveClass();
}
