package com.sa.processor.util;

import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public static final Map<String, Class<?>> classMap = new HashMap<>(8);
    static {
        classMap.put("int", int.class);
        classMap.put("double", double.class);
        classMap.put("long", long.class);
        classMap.put("short", short.class);
        classMap.put("byte", byte.class);
        classMap.put("boolean", boolean.class);
        classMap.put("char", char.class);
        classMap.put("float", float.class);
        classMap.put("void", void.class);
    }
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> clazz = classMap.get(className);
        return clazz == null ? Class.forName(className) : clazz;
    }
}
