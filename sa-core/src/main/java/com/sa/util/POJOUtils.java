package com.sa.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class POJOUtils {

    private static final Logger log = LoggerFactory.getLogger(POJOUtils.class);

    private static final String INVALID_METHOD_NAME = "{0}不符合POJO规范要求!无法根据方法名计算Bean字段的名称";

    private static final String NO_SUPPORT_PRIMITIVE = "当前还没实现对基本类型{0}缺省值的支持!";

    private static final String IS = "is";

    private static final String SET = "set";

    private static final String GET = "get";


    public final static boolean isBeanMethod(Method method) {
        return isBeanMethod(method.getName());
    }


    public final static boolean isBeanMethod(String methodName) {
        return methodName.startsWith(GET) || methodName.startsWith(SET) || methodName.startsWith(IS);
    }


    public final static boolean isGetOrSetMethod(String methodName) {
        return methodName.startsWith(GET) || methodName.startsWith(SET);
    }


    public final static boolean isGetMethod(Method method) {
        return method.getName().startsWith(GET) || method.getName().startsWith(IS);
    }


    public final static boolean isGetMethod(String methodName) {
        return methodName.startsWith(GET) || methodName.startsWith(IS);
    }


    public final static boolean isSetMethod(Method method) {
        return method.getName().startsWith(SET);
    }


    public final static boolean isSetMethod(String methodName) {
        return methodName.startsWith(SET);
    }


    public final static boolean isISMethod(Method method) {
        return method.getName().startsWith(IS);
    }


    public final static boolean isISMethod(String methodName) {
        return methodName.startsWith(IS);
    }


    public final static String getBeanField(Method method) {
        return getBeanField(method.getName());
    }


    public final static String getBeanField(String methodName) {
        if (isGetOrSetMethod(methodName)) {
            return toBeanFieldName(methodName.substring(3));
        } else if (isISMethod(methodName)) {
            return toBeanFieldName(methodName.substring(2));
        } else {
            throw new RuntimeException(MessageFormat.format(INVALID_METHOD_NAME, methodName));
        }
    }


    public final static String toBeanFieldName(String name) {
        return Introspector.decapitalize(name);
    }


    public final static Object getPrimitiveValue(Class<?> primitiveClz, Object value) {
        assert (primitiveClz != null);
        assert (primitiveClz.isPrimitive());

        if (value == null) {
            return getPrimitiveDefault(primitiveClz);
        }
        return ConvertUtils.convert(value.toString(), primitiveClz);
    }


    public final static Object getPrimitiveDefault(Class<?> primitiveClz) {
        assert (primitiveClz != null);
        assert (primitiveClz.isPrimitive());
        if (int.class == primitiveClz)
            return 0;
        else if (long.class == primitiveClz)
            return 0;
        else if (double.class == primitiveClz)
            return 0.00;
        else if (float.class == primitiveClz)
            return 0.00f;
        else if (boolean.class == primitiveClz)
            return false;

        else {
            String message = MessageFormat.format(NO_SUPPORT_PRIMITIVE, primitiveClz);
            log.debug(message);
            throw new RuntimeException(message);
        }
    }


    @SuppressWarnings("unchecked")
    public final static boolean hasProperty(Object object, String name) {
        if (object instanceof Map) {
            return ((Map) object).containsKey(name);
        }
        return PropertyUtils.isReadable(object, name);
    }


    public final static Object getProperty(Object object, String name) {
        assert (name != null);

        if (object != null) {
            try {
                return PropertyUtils.getProperty(object, name);
            } catch (Exception e) {
                log.debug(MessageFormat.format("取{0}属性的值时出错,错误消息:{1}", name, e.getMessage()));
            }
        }
        return null;
    }


    public static void setProperty(Object object, String name, Object value) {
        assert (name != null);
        if (object != null) {
            try {
                PropertyUtils.setProperty(object, name, value);
            } catch (Exception e) {
                log.debug(MessageFormat.format("设置{0}属性的值时出错，错误消息:{1}", name, e.getMessage()));
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static Object getStaticField(Class clazz, String fieldName) {
        Field[] fields = clazz.getFields();
        if (fields != null) {
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getName().equals(fieldName)) {
                    try {
                        return field.get(null);
                    } catch (Exception ex) {
                        log.debug(MessageFormat.format("取静态字段{0}时出错，错误消息:{1}", fieldName, ex.getMessage()));
                    }
                }
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public final static boolean isAssignableFrom(Class clazz1, String clazz2Name) {
        if (clazz1 == null || clazz2Name == null)
            return false;
        try {
            return isAssignableFrom(clazz1, ClassUtils.getClass(clazz2Name));
        } catch (Exception e) {
            log.error("检查两个类的继承和实现关系时出错,错误消息：", e);
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public static boolean isAssignableFrom(String clazz1Name, Class clazz2) {
        if (clazz1Name == null || clazz2 == null)
            return false;
        try {
            return isAssignableFrom(ClassUtils.getClass(clazz1Name), clazz2);
        } catch (Exception e) {
            log.debug(MessageFormat.format("检查两个类的继承和实现关系时出错,错误消息：", e.getMessage()));
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public static boolean isAssignableFrom(String clazz1Name, String clazz2Name) {
        if (clazz1Name == null || clazz2Name == null)
            return false;
        try {
            return isAssignableFrom(ClassUtils.getClass(clazz1Name), ClassUtils.getClass(clazz2Name));
        } catch (Exception e) {
            log.debug(MessageFormat.format("检查两个类的继承和实现关系时出错,错误消息：", e.getMessage()));
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public final static boolean isAssignableFrom(Class clazz1, Class clazz2) {
        return clazz1 == null || clazz2 == null ? false : clazz1.isAssignableFrom(clazz2);
    }


    @SuppressWarnings("unchecked")
    public final static boolean isBaseTypeClass(Class clazz) {
        if (clazz == String.class || clazz == Date.class)
            return true;
        if (clazz.isPrimitive())
            return true;
        return clazz.getPackage() != null && "java.lang".equals(clazz.getPackage().getName());
    }



    private static Pattern linePattern = Pattern.compile("_(\\w)");

    public static String lineToHump(String str){
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String humpToLine(String str){
        return str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }
    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static String humpToLineFast(String str){
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, "_"+matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.indexOf("_") == 0 ? sb.substring(1) : sb.toString();
    }

}
