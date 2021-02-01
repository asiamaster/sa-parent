package com.sa.oplog.provider;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sa.base.BaseService;
import com.sa.dto.DTO;
import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.exception.ParamErrorException;
import com.sa.metadata.annotation.FieldDef;
import com.sa.oplog.base.LogContentProvider;
import com.sa.oplog.dto.LogContext;
import com.sa.oplog.dto.UpdatedLogInfo;
import com.sa.util.BeanConver;
import com.sa.util.DateUtils;
import com.sa.util.POJOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


@Component
public class UpdateLogContentProvider implements LogContentProvider {



    @Autowired
    private Map<String, BaseService> serviceMap;


    @Override
    public String content(Method method, Object[] args, String params, LogContext logContext){

        Object updatedBean = args[0];

        Class<?> clazz = DTOUtils.getDTOClass(updatedBean);
        String serviceName = StringUtils.isBlank(params) ? null : params.trim().startsWith("{") ? JSONObject.parseObject(params).getString("serviceName") : params.trim();
        BaseService<? extends IDTO, Long> service = null;
        if(StringUtils.isBlank(serviceName)){
            service = serviceMap.get(Introspector.decapitalize(clazz.getSimpleName())+"ServiceImpl");
        }else{
            service = serviceMap.get(serviceName);
        }

        Map<String, UpdatedLogInfo> updatedFields = new HashMap<>();
        List<String> excludes = Lists.newArrayList("id", "page", "sort", "rows", "order", "metadata"
                , "setForceParams", "insertForceParams", "selectColumns", "whereSuffixSql", "checkInjection");
        Object id = null;

        if(clazz.isInterface()) {
            id = buildUpdatedFieldsByDto(updatedFields, clazz, updatedBean, service, excludes);
        }

        else{
            id = buildUpdatedFieldsByBean(updatedFields, clazz, updatedBean, service, excludes);
        }
        StringBuilder stringBuilder = new StringBuilder("[目标id]:"+id+"\r\n");
        if(updatedFields.isEmpty()){
            stringBuilder.append("无字段修改");
        }else {
            for (String key : updatedFields.keySet()) {
                UpdatedLogInfo updatedLogInfo = updatedFields.get(key);
                Object oldValue = updatedLogInfo.getOldValue();
                if (oldValue == null) {
                    stringBuilder.append("[" + updatedLogInfo.getLabel() + "]:修改为'" + updatedLogInfo.getNewValue() + "'\r\n");
                } else {
                    if (oldValue instanceof Date) {
                        oldValue = DateUtils.format((Date) oldValue);
                    }
                    stringBuilder.append("[" + updatedLogInfo.getLabel() + "]:从'" + oldValue + "'修改为'" + updatedLogInfo.getNewValue() + "'\r\n");
                }
            }
        }
        return stringBuilder.toString();
    }


    private Object buildUpdatedFieldsByDto(Map<String, UpdatedLogInfo> updatedFields, Class<?> clazz, Object updatedBean, BaseService<? extends IDTO, Long> service, List<String> excludes){
        DTO updatedDto = DTOUtils.go(updatedBean);
        Object idObj = updatedDto.get("id");
        IDTO oldObj = null;

        if(service != null && idObj != null){
            oldObj = service.get(Long.parseLong(idObj.toString()));
        }
        DTO oldObjDTO = DTOUtils.go(oldObj);
        Method[] methods = clazz.getMethods();
        for (Method dtoMethod : methods) {
            if (POJOUtils.isGetMethod(dtoMethod)) {
                Transient aTransient = dtoMethod.getAnnotation(Transient.class);
                if(aTransient != null){
                    continue;
                }
                FieldDef fieldDef = dtoMethod.getAnnotation(FieldDef.class);
                String field = POJOUtils.getBeanField(dtoMethod);
                if(excludes.contains(field)){
                    continue;
                }
                String label = fieldDef == null ? field : fieldDef.label();
                Object value = updatedDto.get(field);
                if(null != value){
                    if(oldObj != null){

                        if(Objects.equals(value, oldObjDTO.get(field))){
                            continue;
                        }
                    }
                    UpdatedLogInfo updatedLogInfo = new UpdatedLogInfo();
                    updatedLogInfo.setOldValue(oldObjDTO.get(field));
                    updatedLogInfo.setNewValue(value);
                    updatedLogInfo.setLabel(label);
                    updatedFields.put(field, updatedLogInfo);
                }
            }
        }
        return idObj;
    }


    private Object buildUpdatedFieldsByBean(Map<String, UpdatedLogInfo> updatedFields, Class<?> clazz, Object param1, BaseService<? extends IDTO, Long> service, List<String> excludes){
        Map<String, Object> objMap = null;
        try {
            objMap = BeanConver.transformObjectToMap(param1);
        } catch (Exception e) {
            throw new ParamErrorException(e);
        }

        Map<String, Object> oldObjMap = null;
        if(service != null && objMap != null && objMap.get("id") != null){
            Object oldBean = service.get(Long.parseLong(objMap.get("id").toString()));
            if(oldBean != null){
                try {
                    oldObjMap = BeanConver.transformObjectToMap(oldBean);
                } catch (Exception e) {
                }
            }
        }
        Field[] fields = clazz.getFields();
        for(Field field : fields){
            Transient aTransient = field.getAnnotation(Transient.class);
            if(aTransient != null){
                continue;
            }
            if(excludes.contains(field.getName())){
                continue;
            }
            FieldDef fieldDef = field.getAnnotation(FieldDef.class);
            String label = fieldDef == null ? field.getName() : fieldDef.label();
            field.setAccessible(true);
            try {
                Object value = field.get(param1);
                if(null != value){
                    UpdatedLogInfo updatedLogInfo = new UpdatedLogInfo();
                    updatedLogInfo.setNewValue(value);
                    updatedLogInfo.setLabel(label);
                    if(oldObjMap != null){

                        if(value.equals(oldObjMap.get(field))){
                            continue;
                        }
                        updatedLogInfo.setOldValue(oldObjMap.get(field));
                    }
                    updatedFields.put(field.getName(), updatedLogInfo);
                }
            } catch (IllegalAccessException e) {
            }
        }
        return objMap.get("id");
    }
}
