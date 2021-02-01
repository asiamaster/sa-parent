package com.sa.metadata;

import java.util.List;
import java.util.Map;


public interface ValueProvider {

    String EMPTY_ITEM_TEXT = "-- 请选择 --";

    String EMPTY_ITEM_TEXT_KEY = "emptyText";

    String REQUIRED_KEY = "required";

    String QUERY_PARAMS_KEY = "queryParams";

    String FIELD_KEY = "field";

    String ROW_DATA_KEY = "_rowData";

    String INDEX_KEY = "index";

    String PROVIDER_KEY = "provider";

    String EXTRA_PARAMS_KEY = "_extraParams";
    
    List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta);

    
    String getDisplayText(Object val, Map metaMap, FieldMeta fieldMeta);


}
