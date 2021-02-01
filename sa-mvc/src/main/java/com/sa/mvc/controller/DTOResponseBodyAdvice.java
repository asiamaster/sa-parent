package com.sa.mvc.controller;

import com.sa.domain.BaseOutput;
import com.sa.dto.DTO;
import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.mvc.annotation.Cent2Yuan;
import com.sa.util.BeanConver;
import com.sa.util.POJOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
@ConditionalOnExpression("'${responseBodyAdvice.enable}'=='true'")
public class DTOResponseBodyAdvice implements ResponseBodyAdvice {

    protected static final Logger log = LoggerFactory.getLogger(DTOResponseBodyAdvice.class);


    public static final Map<Method, List<Object>> cent2YuanMethodFieldCache = new HashMap<>();

    @Override
    public Object beforeBodyWrite(Object returnValue, MethodParameter methodParameter,
                                  MediaType mediaType, Class clas, ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {


        if(returnValue == null){
            return null;
        }
        List<Object> cent2YuanMethodFields = cent2YuanMethodFieldCache.get(methodParameter.getMethod());

        if(returnValue instanceof List){
            List<IDTO> retList = (List)returnValue;
            return handleListDto(retList, cent2YuanMethodFields);
        }
        else if(returnValue instanceof IDTO){
            List<Object> cent2YuanMethods = cent2YuanMethodFieldCache.get(methodParameter.getMethod());
            if(cent2YuanMethods.isEmpty()){
                return returnValue;
            }
            try {
                if(DTOUtils.getDTOClass(returnValue).isInterface()) {
                    return handleDto((IDTO) returnValue, (List) cent2YuanMethods);
                }else{
                    return handleBean((IDTO) returnValue, (List) cent2YuanMethods);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return returnValue;
            }
        }
        else if(returnValue instanceof BaseOutput){
            try {
                return handleBaseOutput(methodParameter.getMethod(), (BaseOutput)returnValue);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return returnValue;
            }
        }

        return returnValue;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class clazz) {
        return cent2YuanMethodFieldCache.containsKey(methodParameter.getMethod());

























































    }


    private Object handleListDto(List<IDTO> retList, List<Object> cent2YuanMethodFields){
        if(retList == null || retList.isEmpty()){
            return retList;
        }
        List<Map> dtos = new ArrayList<>(retList.size());
        if(cent2YuanMethodFields.get(0) instanceof Method){
            for (IDTO idto : retList){
                try {
                    dtos.add(handleDto(idto, (List)cent2YuanMethodFields));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        }else{
            for (IDTO idto : retList){
                try {
                    dtos.add(handleBean(idto, (List)cent2YuanMethodFields));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        }
        return dtos;
    }


    private DTO handleDto(IDTO idto, List<Method> cent2YuanMethods) throws InvocationTargetException, IllegalAccessException {
        DTO dto = DTOUtils.go(idto);
        for(Method getMethod : cent2YuanMethods){
            Long cent = (Long)getMethod.invoke(idto);
            if(cent == null){
                continue;
            }
            dto.put(POJOUtils.getBeanField(getMethod), cent2Yuan(cent));
        }
        return dto;
    }


    private Map handleBean(IDTO idto, List<Field> cent2YuanFields) throws Exception {
        Map dto = BeanConver.transformObjectToMap(idto);
        for(Field field : cent2YuanFields){
            Long cent = (Long)field.get(idto);
            if(cent == null){
                continue;
            }
            dto.put(field, cent2Yuan(cent));
        }
        return dto;
    }


    private BaseOutput handleBaseOutput(Method method, BaseOutput baseOutput) throws InvocationTargetException, IllegalAccessException {
        Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
        List<Object> cent2YuanMethodFields = cent2YuanMethodFieldCache.get(method);

        if((parameterizedType[0] instanceof Class)){
            return baseOutput.setData(handleDto((IDTO) baseOutput.getData(), (List)cent2YuanMethodFields));
        }
        else{
            return baseOutput.setData(handleListDto((List)baseOutput.getData(), cent2YuanMethodFields));
        }
    }


    private List<Method> getCent2YuanMethods(Class clazz){
        List<Method> cent2YuanMethods = new ArrayList<>();
        for(Method method : clazz.getMethods()){

            if(POJOUtils.isGetMethod(method) && Long.class == method.getReturnType()) {
                Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
                if (cent2Yuan != null) {
                    cent2YuanMethods.add(method);
                }
            }
        }
        return cent2YuanMethods;
    }


    private String cent2Yuan(Long cent){
        return new BigDecimal(cent).divide(new BigDecimal(100)).toString();
    }

}
