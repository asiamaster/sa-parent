package com.sa.quartz.provider;

import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValuePairImpl;
import com.sa.metadata.ValueProvider;
import com.sa.quartz.domain.QuartzConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class JobStatusProvider implements ValueProvider {
    private static final List<ValuePair<?>> buffer;

    static {
        buffer = new ArrayList<ValuePair<?>>();
        QuartzConstants.JobStatus[] jobStatuses = QuartzConstants.JobStatus.values();
        for(QuartzConstants.JobStatus jobStatus : jobStatuses){
            buffer.add(new ValuePairImpl(jobStatus.getDesc(), jobStatus.getCode()));
        }






    }

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return buffer;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) return null;
        for(ValuePair<?> valuePair : buffer){
            if(obj.equals(valuePair.getValue())){
                return valuePair.getText();
            }
        }
        return null;
    }
}