package com.sa.metadata.provider;

import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValueProvider;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class DateProvider implements ValueProvider {

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) {
            return "";
        }
        if(obj instanceof IDTO){
            String field = (String)metaMap.get(ValueProvider.FIELD_KEY);
            field = field.substring(field.lastIndexOf(".") + 1, field.length());

            if(DTOUtils.isProxy(obj)){
                obj = ((IDTO)obj).aget(field);
            }else{
                try {
                    obj = PropertyUtils.getProperty(obj, field);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return convertDate(obj);
    }


    private String convertDate(Object obj){
        if(obj instanceof LocalDate){

            return obj.toString();
        }
        if(obj instanceof LocalDateTime){

            return ((LocalDateTime)obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()));
        }
        if(obj instanceof Date){
            return new SimpleDateFormat("yyyy-MM-dd").format((Date)obj);
        }
        Long time = obj instanceof Long ? (Long)obj : obj instanceof String ? Long.parseLong(obj.toString()) : 0;
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
    }
}
