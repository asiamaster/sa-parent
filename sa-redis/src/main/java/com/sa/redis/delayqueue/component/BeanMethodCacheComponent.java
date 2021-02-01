package com.sa.redis.delayqueue.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component
public class BeanMethodCacheComponent {


    private Map<Object, Method> map = new HashMap<>();
    @Autowired
    private ApplicationContext applicationContext;


    public Map<Object, Method> getBeanMethod(Class<? extends Annotation> annotationClass) {
        if (!this.map.isEmpty()) {
            return this.map;
        }
        Map<Object, Method> map = new HashMap<>();
        String[] beans = applicationContext.getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> clazz = applicationContext.getType(beanName);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                boolean present = method.isAnnotationPresent(annotationClass);
                if (present) {
                    map.put(applicationContext.getBean(beanName), method);
                    break;
                }
            }
        }
        this.map = map;
        return map;
    }
}
