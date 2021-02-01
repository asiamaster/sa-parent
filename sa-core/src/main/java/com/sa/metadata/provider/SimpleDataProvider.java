package com.sa.metadata.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class SimpleDataProvider implements ValueProvider {

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) return "";
        JSONArray ja = (JSONArray)metaMap.get("data");
        if(ja == null || ja.isEmpty()) return "";
        for(Object o : ja){
            JSONObject jo = (JSONObject)o;

            if(jo.get("value") == null){
                continue;
            }
            if(jo.get("value").equals(obj) || jo.get("value").equals(obj.toString())){
                return jo.get("text").toString();
            }
        }
        return "";
    }
}