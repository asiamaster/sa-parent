package com.sa.mvc.boot;

import com.sa.domain.BaseOutput;
import com.sa.dto.IDTO;
import com.sa.mvc.annotation.Cent2Yuan;
import com.sa.mvc.controller.DTOResponseBodyAdvice;
import com.sa.util.AopTargetUtils;
import com.sa.util.POJOUtils;
import com.sa.util.SpringUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@ConditionalOnExpression("'${responseBodyAdvice.enable}'=='true'")
public class ControllerResponseApplicationListener implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Map<String, Object> controllerBeans = SpringUtil.getBeansWithAnnotation(Controller.class);
        if(controllerBeans == null){
            controllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
        }else {
            Map<String, Object> restControllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
            if(restControllerBeans != null) {
                controllerBeans.putAll(restControllerBeans);
            }
        }
        if(controllerBeans == null || controllerBeans.isEmpty()){
            return;
        }

        for(Map.Entry<String, Object> entry : controllerBeans.entrySet()){
            try {
                Class<?> controllerClass = AopTargetUtils.getTarget(entry.getValue()).getClass();

                for(Method method : controllerClass.getMethods()){

                    if(supportsCent2Yuan(method)){
                        List<Object> methodFields = getCent2YuanMethodFields(getReturnTypeDTO(method));
                        if(methodFields.isEmpty()){
                            continue;
                        }
                        DTOResponseBodyAdvice.cent2YuanMethodFieldCache.put(method, methodFields);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }


    private boolean supportsCent2Yuan(Method method) {

        if(List.class.isAssignableFrom(method.getReturnType()) && method.getGenericReturnType() != null) {

            Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
            if(parameterizedType == null || parameterizedType.length == 0 || !(parameterizedType[0] instanceof Class)){
                return false;
            }






            if(IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
                return true;
            }
            return false;
        }

        else if(IDTO.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }

        else if(BaseOutput.class.isAssignableFrom(method.getReturnType())) {

            if(method.getGenericReturnType() instanceof ParameterizedTypeImpl){
                Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
                if(parameterizedType == null || parameterizedType.length == 0){
                    return false;
                }

                if((parameterizedType[0] instanceof Class) && IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
                    return true;
                }

                if(parameterizedType[0] instanceof ParameterizedTypeImpl) {
                    Type[] parameterizedType1 =  ((ParameterizedTypeImpl) parameterizedType[0]).getActualTypeArguments();
                    if(parameterizedType1 == null || parameterizedType1.length == 0){
                        return false;
                    }

                    if(parameterizedType1[0] instanceof ParameterizedTypeImpl){
                        return false;
                    }

                    if (List.class.isAssignableFrom(((ParameterizedTypeImpl) parameterizedType[0]).getRawType())) {
                        Class parameterizedClass = (Class) parameterizedType1[0];

                        if (IDTO.class.isAssignableFrom(parameterizedClass)) {
                            return true;
                        }
                        return false;
                    }
                }
                return false;
            }
            else{
                return false;
            }
        }
        return false;
    }


    private Class getReturnTypeDTO(Method method){

        if(IDTO.class.isAssignableFrom(method.getReturnType())) {
            return (Class) method.getReturnType();
        }
        else if(List.class.isAssignableFrom(method.getReturnType()) && method.getGenericReturnType() != null) {
            return (Class)((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments()[0];
        }else if(BaseOutput.class.isAssignableFrom(method.getReturnType())) {
            Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();

            if((parameterizedType[0] instanceof Class)){
                return (Class) parameterizedType[0];
            }
            else if(parameterizedType[0] instanceof ParameterizedTypeImpl) {
                Type[] parameterizedType1 =  ((ParameterizedTypeImpl) parameterizedType[0]).getActualTypeArguments();

                if (List.class.isAssignableFrom(((ParameterizedTypeImpl) parameterizedType[0]).getRawType())) {
                    return (Class) parameterizedType1[0];
                }
            }
        }
        return null;
    }

    private List<Object> getCent2YuanMethodFields(Class clazz){
        List<Object> cent2YuanMethodFields = new ArrayList<>();
        if(clazz.isInterface()){
            for(Method method : clazz.getMethods()){

                if(POJOUtils.isGetMethod(method) && Long.class == method.getReturnType()) {
                    Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
                    if (cent2Yuan != null) {
                        cent2YuanMethodFields.add(method);
                    }
                }
            }
        }else{
            for(Field field : clazz.getFields()){

                if(Long.class == field.getType()) {
                    Cent2Yuan cent2Yuan = field.getAnnotation(Cent2Yuan.class);
                    if (cent2Yuan != null) {
                        cent2YuanMethodFields.add(field);
                    }
                }
            }
        }
        return cent2YuanMethodFields;
    }

}
