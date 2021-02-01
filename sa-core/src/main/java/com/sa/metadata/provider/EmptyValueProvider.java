package com.sa.metadata.provider;

import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class EmptyValueProvider implements ValueProvider {
	@Override
	public String getDisplayText(Object val, Map metadata, FieldMeta fieldMeta) {
		return "";
	}

	@Override
    public List<ValuePair<?>> getLookupList(Object val, Map metadata, FieldMeta fieldMeta) {
		return null;
	}
}
