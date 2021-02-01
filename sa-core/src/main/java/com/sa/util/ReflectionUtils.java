package com.sa.util;



import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;


public class ReflectionUtils {

    private static Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    
    public static Object invokeGetterMethod(Object obj, String propertyName) {
        String getterMethodName = "get" + StringUtils.capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
    }

    
    public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
        invokeSetterMethod(obj, propertyName, value, null);
    }

    
    public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
        Class<?> type = propertyType != null ? propertyType : value.getClass();
        String setterMethodName = "set" + StringUtils.capitalize(propertyName);
        invokeMethod(obj, setterMethodName, new Class[] { type }, new Object[] { value });
    }

    
    public static Object getFieldValue(final Object obj, final String fieldName) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    
    public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常:{}", e.getMessage());
        }
    }

    
    public static List<Field> getAccessibleFields(final Class clazz, boolean excludeStatic, boolean excludeFinal) {
        Assert.notNull(clazz, "clazz不能为空");
        List<Field> fields = Lists.newArrayList();
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            Field[] declaredFields = superClass.getDeclaredFields();
            for(Field field : declaredFields){
                if("serialVersionUID".equals(field.getName())){
                    continue;
                }

                if(excludeStatic && Modifier.isStatic(field.getModifiers())){
                    continue;
                }

                if(excludeFinal && Modifier.isFinal(field.getModifiers())){
                    continue;
                }
                if(!fields.contains(field)){
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        Assert.notNull(obj, "object不能为空");
        Assert.hasText(fieldName, "fieldName");
        return getAccessibleField(obj.getClass(), fieldName);
    }

    
    public static Field getAccessibleField(final Class clazz, final String fieldName) {
        Assert.notNull(clazz, "clazz不能为空");
        Assert.hasText(fieldName, "fieldName");
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {

            }
        }
        return null;
    }

    
    public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    
    public static Method getAccessibleMethod(final Object obj, final String methodName,
                                             final Class<?>... parameterTypes) {
        Assert.notNull(obj, "object不能为空");
        return getAccessibleMethod(obj.getClass(), methodName, parameterTypes);
    }

    
    public static Method getAccessibleMethod(final Class clazz, final String methodName,
                                             final Class<?>... parameterTypes) {
        Assert.notNull(clazz, "clazz不能为空");
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            try {
                Method method = superClass.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {

            }
        }

        if(clazz.isInterface()) {
            return getIntefacesAccessibleMethod(clazz, methodName, parameterTypes);
        }else{
            return null;
        }
    }

    
    public static List<Method> getAccessibleMethods(Class<?> intfClasses){
        List<Method> methods = new ArrayList<>();
        getAccessibleMethodsRecursive(methods, intfClasses);
        return methods;
    }

    
    public static Method getIntefacesAccessibleMethod(final Class<?> intfClass,final String methodName, final Class<?>... parameterTypes){
        Assert.notNull(intfClass, "intfClasses不能为空");
        try {
            Method method = intfClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {

        }
        for (Class<?> clazz : intfClass.getInterfaces()) {
            return getIntefacesAccessibleMethod(clazz, methodName, parameterTypes);
        }
        return null;
    }

    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenricType(final Class clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    
    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException("Reflection Exception.", e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Unexpected Checked Exception.", e);
    }

    
    public static String getCurrentMethodName(){
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String getJavaVersion(){
        return System.getProperty("java.version");
    }

    
    public static boolean isJava8(){
        if (getJavaVersion().contains("1.8.")) {
            return true;
        }
        return false;
    }



    
    private static void getAccessibleMethodsRecursive(List<Method> methods, Class<?> intfClasses){
        Assert.notNull(intfClasses, "intfClasses不能为空");
        Assert.notNull(methods, "methods不能为空");
        for(Method method : intfClasses.getDeclaredMethods()){
            method.setAccessible(true);
            methods.add(method);
        }
        for (Class<?> intfClass : intfClasses.getInterfaces()) {
            getAccessibleMethodsRecursive(methods, intfClass);
        }
    }



    
    public static final int MAX_NEST_DEPTH = 20;

    
    public static Class<?> getClassByName(String className) throws ClassNotFoundException {
        return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    }

    
















    
    public static Object invokeMethod(Object target, String methodName)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class<?> cl = target.getClass();
        int i = 0;
        while ((i++) < MAX_NEST_DEPTH && cl != null) {
            try {
                Method m = cl.getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (Exception e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException("class:" + target.getClass() + ", methodName:" + methodName);
    }

    

















    
    public static Object invokeStaticMethod(Class<?> targetClass, String methodName, Class<?>[] parameterTypes,
                                            Object[] parameterValues)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        int i = 0;
        while ((i++) < MAX_NEST_DEPTH && targetClass != null) {
            try {
                Method m = targetClass.getMethod(methodName, parameterTypes);
                return m.invoke(null, parameterValues);
            } catch (Exception e) {
                targetClass = targetClass.getSuperclass();
            }
        }
        throw new NoSuchMethodException("class:" + targetClass + ", methodName:" + methodName);
    }

    
    public static Method getMethod(Class<?> classType, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException, SecurityException {
        return classType.getMethod(methodName, parameterTypes);
    }

    
    public static Set<Class<?>> getInterfaces(Class<?> clazz) {
        if (clazz.isInterface()) {
            return Collections.singleton(clazz);
        }
        Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        while (clazz != null) {
            Class<?>[] ifcs = clazz.getInterfaces();
            for (Class<?> ifc : ifcs) {
                interfaces.addAll(getInterfaces(ifc));
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    
    public static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);

        Class<?> declaringClass = method.getDeclaringClass();
        int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
        return constructor.newInstance(declaringClass, allModes)
                .unreflectSpecial(method, declaringClass)
                .bindTo(proxy)
                .invokeWithArguments(args);








    }
}

