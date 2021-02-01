package com.sa.metadata;

import java.util.List;
import java.util.Map;


public interface BatchValueProvider extends ValueProvider {


    void setDisplayList(List list, Map metaMap, ObjectMeta fieldMeta);


}
