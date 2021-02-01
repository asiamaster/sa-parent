package com.sa.metadata.provider;

import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.glossary.BeanType;
import com.sa.metadata.*;
import com.sa.metadata.handler.DefaultMismatchHandler;
import com.sa.util.BeanConver;
import com.sa.util.POJOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Component
public abstract class BatchDisplayTextProviderAdaptor implements BatchValueProvider {
    protected static final Logger log = LoggerFactory.getLogger(BatchDisplayTextProviderAdaptor.class);

    protected static final String ESCAPE_FILEDS_KEY = "_escapeFileds";

    protected static final String FK_FILED_KEY = "_fkField";

    @Autowired
    protected DefaultMismatchHandler defaultMismatchHandler;


    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public void setDisplayList(List list, Map metaMap, ObjectMeta fieldMeta) {
        if (CollectionUtils.isEmpty(metaMap) || CollectionUtils.isEmpty(list)) {
            return;
        }

        if (metaMap.containsKey(FIELD_KEY)){
            String field = (String)metaMap.get(FIELD_KEY);
            Map<String, String> escapeFields = getEscapeFileds(metaMap);

            for(Map.Entry<String, String> entry : escapeFields.entrySet()){
                if(entry.getKey().equals(field)){
                    int size = list.size();
                    int capacity = size/2 < 10 ? size : size/2;

                    List<String> relationIds = buildRelationIdList(list, capacity, metaMap);
                    if(relationIds.isEmpty()){
                        break;
                    }

                    List relationDatas = getFkList(relationIds, metaMap);
                    if(relationDatas == null || relationDatas.isEmpty()){

                        populateMismatchData(relationDatas, escapeFields, list, metaMap);
                        break;
                    }

                    Map<String, Map> id2RelTable = new HashMap<>(relationDatas.size());

                    String relactionTablePkField = getRelationTablePkField(metaMap);
                    boolean ignoreCaseToRef = ignoreCaseToRef(metaMap);
                    for(Object obj : relationDatas){
                        try {
                            Map map = BeanConver.transformObjectToMap(obj);

                            Object relationTablePkFieldValue = map.get(relactionTablePkField);
                            if(relationTablePkFieldValue != null) {

                                if(ignoreCaseToRef) {
                                    id2RelTable.put(relationTablePkFieldValue.toString().toLowerCase(), map);
                                }else{
                                    id2RelTable.put(relationTablePkFieldValue.toString(), map);
                                }
                            }
                        } catch (Exception e) {
                            log.error("批量提供者转换(getFkList方法的结果)失败:"+e.getLocalizedMessage());
                            break;
                        }
                    }

                    setDtoData(list, id2RelTable, metaMap);
                    break;
                }
            }
        }
    }


    public void populateMismatchData(List relationDatas, Map<String, String> escapeFields, List list, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }

        BeanType beanType = BeanType.JAVA_BEAN;
        if(list.get(0) instanceof IDTO && list.get(0).getClass().isInterface()){
            beanType = BeanType.DTO;
        }else if(list.get(0) instanceof Map){
            beanType = BeanType.MAP;
        }
        for (Object obj : list) {
            Map map = (Map) obj;
            Object keyObj = null;
            if(BeanType.DTO == beanType){
                keyObj = ((IDTO)obj).aget(fkField);
            }else if(BeanType.MAP.equals(beanType)) {
                keyObj = map.get(fkField);
            }else{
                keyObj = POJOUtils.getProperty(obj, fkField);
            }

            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }
            for (Map.Entry<String, String> escapeFieldEntry : escapeFields.entrySet()) {

                map.put(escapeFieldEntry.getKey(), getMismatchHandler(metaMap).apply(keyObj));
            }
        }
    }


    private List<String> buildRelationIdList(List list, int capacity, Map metaMap){
        List<String> relationIds = new ArrayList(capacity);
        String fkField = getFkField(metaMap);
        if(fkField.contains(".")) {
            String childKey = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            for(Object obj : list) {
                Object fkObj = getObjectValueByKey(obj, fkField);
                if(fkObj == null){
                    continue;
                }
                Object fkValue = getObjectValueByKey(fkObj, childKey);
                if(fkValue == null){
                    continue;
                }
                relationIds.add(fkValue.toString());
            }
        }else {
            for (Object obj : list) {
                Object fkValue = getObjectValueByKey(obj, fkField);
                if (fkValue == null) {
                    continue;
                }
                relationIds.add(fkValue.toString());
            }
        }
        return relationIds;
    }


    private Object getObjectValueByKey(Object obj, String key){
        if(obj instanceof Map){
            Map map = (Map)obj;
            return map.get(key);
        }else if(IDTO.class.isAssignableFrom(DTOUtils.getDTOClass(obj))){

            if(DTOUtils.isProxy(obj)){
                return ((IDTO) obj).aget(key);
            }
            else{
                return POJOUtils.getProperty(obj, key);
            }
        }

        return POJOUtils.getProperty(obj, key);
    }


    protected boolean ignoreCaseToRef(Map metaMap){
        return false;
    }


    protected abstract List getFkList(List<String> relationIds, Map metaMap);








    protected Map<String, String> getEscapeFileds(Map metaMap){
        if(metaMap.get(ESCAPE_FILEDS_KEY) instanceof Map){
            return (Map)metaMap.get(ESCAPE_FILEDS_KEY);
        }else {
            Map<String, String> map = new HashMap<>();
            map.put(metaMap.get(FIELD_KEY).toString(), getEscapeFiled(metaMap));
            return map;
        }
    }


    protected String getEscapeFiled(Map metaMap){return null;};



    protected String getFkField(Map metaMap) {
        String field = (String)metaMap.get(FIELD_KEY);
        String fkField = (String)metaMap.get(FK_FILED_KEY);
        return fkField == null ? field : fkField;
    }


    protected String getRelationTablePkField(Map metaMap) {
        return "id";
    }


    protected Function getMismatchHandler(Map metaMap){
        return defaultMismatchHandler;
    }


    private void setDtoData(List list, Map<String, Map> id2RelTable, Map metaMap){
        if(list.get(0) instanceof IDTO && list.get(0).getClass().isInterface()) {
            handleDtoData(list, id2RelTable, metaMap);
        }else if(list.get(0) instanceof Map) {
            handleMapData(list, id2RelTable, metaMap);
        }else{
            handleBeanData(list, id2RelTable, metaMap);
        }
    }


    private void handleDtoData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }
        for (Object obj : list) {
            IDTO dto = (IDTO) obj;

            Object keyObj = dto.aget(fkField);

            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }
            String key = keyObj.toString();

            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            Function mismatchHandler = getMismatchHandler(metaMap);
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {

                if(hasChild){

                    String originalKey = new StringBuilder(fkField).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
                    dto.aset(originalKey, keyObj);
                }else {
                    dto.aset(ValueProviderUtils.ORIGINAL_KEY_PREFIX + entry.getKey(), keyObj);
                }

                if(id2RelTable.get(key) == null){
                    dto.aset(entry.getKey(), mismatchHandler.apply(keyObj));
                }else {
                    dto.aset(entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }


    private void handleMapData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }
        for (Object obj : list) {
            Map map = (Map) obj;
            Object keyObj = map.get(fkField);

            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }

            String key = keyObj.toString();

            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {

                if(hasChild){

                    String originalKey = new StringBuilder(fkField).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
                    map.put(originalKey, keyObj);
                }else {
                    map.put(ValueProviderUtils.ORIGINAL_KEY_PREFIX + entry.getKey(), keyObj);
                }

                if(id2RelTable.get(key) == null){
                    map.put(entry.getKey(), getMismatchHandler(metaMap).apply(keyObj));
                }else {
                    map.put(entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }


    private void handleBeanData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }

        for (Object obj : list) {
            Object keyObj = POJOUtils.getProperty(obj, fkField);

            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }

            String key = keyObj.toString();

            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {

                if(id2RelTable.get(key) == null){
                    POJOUtils.setProperty(obj, entry.getKey(), getMismatchHandler(metaMap).apply(keyObj));
                }else {

                    POJOUtils.setProperty(obj, entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }

}