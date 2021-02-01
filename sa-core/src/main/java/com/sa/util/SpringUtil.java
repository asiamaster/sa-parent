package com.sa.util;



import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringUtil implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext = null;
    private static Environment environment;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public ApplicationContext applicationContext() {
        return applicationContext;
    }

    public static Environment getEnvironment() {
        return environment;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
        System.out.println("---------------SpringUtil------------------------------------------------------");
        System.out.println("ApplicationContext配置成功,在普通类可以通过调用SpringUtils.getAppContext()获取applicationContext对象,applicationContext=" + SpringUtil.applicationContext);
        System.out.println("--------------------------------------------------------------------------------");
    }



    public static Object getBean(String name) {
        if(getApplicationContext() != null) {
            return getApplicationContext().getBean(name);
        }else{
            return null;
        }
    }


    public static <T> Map<String, T> getBeansOfType(Class<T> clazz){
        return getApplicationContext().getBeansOfType(clazz);
    }


    public static <T> Map<String, T> getBeansOfType(Class<T> clazz, Boolean includeNonSingletons, boolean allowEagerInit){
        return getApplicationContext().getBeansOfType(clazz, includeNonSingletons , allowEagerInit);
    }


    public static <T> T getGenericBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }



    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException{
        return getApplicationContext().getBeansWithAnnotation(annotationType);
    }


    @Override
    public void setEnvironment(Environment environment) {
        if (SpringUtil.environment == null) {
            SpringUtil.environment = environment;
        }
    }

    public static String getProperty(String key){
        return getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue){
        return getEnvironment().getProperty(key, defaultValue);
    }


}