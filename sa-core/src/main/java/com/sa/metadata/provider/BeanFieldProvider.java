package com.sa.metadata.provider;

import com.google.common.collect.Lists;
import com.sa.metadata.*;
import com.sa.util.POJOUtils;
import com.sa.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


@Component
public class BeanFieldProvider implements ValueProvider {

    private static final String QUERY_PARAMS_KEY = "queryParams";


    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        Object className = metaMap.get(QUERY_PARAMS_KEY);
        if(className == null){
            return null;
        }
        List<ValuePair<?>> valuePairs =Lists.newArrayList();
        try {
            Class dtoClass = Class.forName(className.toString());
            ObjectMeta objectMeta = MetadataUtils.getDTOMeta(dtoClass);
            for(Method method : dtoClass.getMethods()) {

                if(POJOUtils.isGetMethod(method)){
                    String fieldName = POJOUtils.getBeanField(method);
                    FieldMeta fieldMeta1 = objectMeta.getFieldMetaById(fieldName);

                    String dbFieldName = null;


                    if(dtoClass.isInterface()){
                        if(method.getAnnotation(Transient.class) != null){
                            continue;
                        }
                        Column column = method.getAnnotation(Column.class);
                        if (column != null) {
                            dbFieldName = column.name();
                        } else {
                            dbFieldName = POJOUtils.humpToLineFast(fieldName);
                        }
                    }else {

                        Field field = ReflectionUtils.getAccessibleField(dtoClass, fieldName);

                        if (field == null || field.getAnnotation(Transient.class) != null) {

                            continue;
                        } else {
                            Column column = field.getAnnotation(Column.class);
                            if (column != null) {
                                dbFieldName = column.name();
                            } else {
                                dbFieldName = POJOUtils.humpToLineFast(fieldName);
                            }
                        }
                    }

                    if(fieldMeta1 == null) {
                        valuePairs.add(new ValuePairImpl<String>(fieldName, dbFieldName));
                    }else{
                        String label = fieldMeta1.getLabel() == null ? fieldMeta1.getName() : fieldMeta1.getLabel();
                        valuePairs.add(new ValuePairImpl<String>(label, dbFieldName));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return valuePairs;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) return "";
        return obj.toString();
    }
}
