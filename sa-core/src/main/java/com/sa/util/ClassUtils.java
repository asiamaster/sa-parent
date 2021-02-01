package com.sa.util;



public class ClassUtils {

    public static Object newInstance(Class<?> c) {
        try {
            return c.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot new instance with class:" + c.getName(), e);
        }
    }

    public static Object newInstance(String className) {
        try {
            return newInstance(Class.forName(className));
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot new instance with className:" + className, e);
        }
    }


    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {

        }
        if (cl == null) {

            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }

}
