package com.sa.metadata.provider;

import com.sa.metadata.BatchProviderMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Component
public abstract class BatchDisplayTextProviderSupport extends BatchDisplayTextProviderAdaptor {
    protected static final Logger log = LoggerFactory.getLogger(BatchDisplayTextProviderSupport.class);


    protected abstract BatchProviderMeta getBatchProviderMeta(Map metaMap);


    @Override
    protected boolean ignoreCaseToRef(Map metaMap){
        Boolean ignoreCaseToRef = getBatchProviderMeta(metaMap).getIgnoreCaseToRef();
        return ignoreCaseToRef == null ? false : ignoreCaseToRef;
    }


    @Override
    protected abstract List getFkList(List<String> relationIds, Map metaMap);








    @Override
    protected Map<String, String> getEscapeFileds(Map metaMap){
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getEscapeFileds() != null){
            return batchProviderMeta.getEscapeFileds();
        }else if(batchProviderMeta.getEscapeFiled() != null){
            Map<String, String> map = new HashMap<>();
            map.put(metaMap.get(FIELD_KEY).toString(), batchProviderMeta.getEscapeFiled());
            return map;
        }
        if(metaMap.get(ESCAPE_FILEDS_KEY) instanceof Map){
            return (Map)metaMap.get(ESCAPE_FILEDS_KEY);
        }else {
            Map<String, String> map = new HashMap<>();
            map.put(metaMap.get(FIELD_KEY).toString(), getEscapeFiled(metaMap));
            return map;
        }
    }


    @Override
    protected String getEscapeFiled(Map metaMap){return null;}



    @Override
    protected String getFkField(Map metaMap) {
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getFkField() != null){
            return batchProviderMeta.getFkField();
        }
        String field = (String)metaMap.get(FIELD_KEY);
        String fkField = (String)metaMap.get(FK_FILED_KEY);
        return fkField == null ? field : fkField;
    }


    @Override
    protected String getRelationTablePkField(Map metaMap) {
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getRelationTablePkField() != null){
            return batchProviderMeta.getRelationTablePkField();
        }
        return "id";
    }

    @Override
    protected Function getMismatchHandler(Map metaMap) {
        return getBatchProviderMeta(metaMap).getMismatchHandler();
    }
}