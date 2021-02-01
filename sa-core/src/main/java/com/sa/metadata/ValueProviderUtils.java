package com.sa.metadata;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.dto.IDomain;
import com.sa.util.BeanConver;
import com.sa.util.POJOUtils;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class ValueProviderUtils {


	public static final String ORIGINAL_KEY_PREFIX = "$_";

	protected static final Logger LOGGER = LoggerFactory.getLogger(ValueProviderUtils.class);
	@Autowired
	private Map<String, ValueProvider> valueProviderMap;


	public static <T extends IDomain> List<Map> buildDataByProvider(T domain, List list) throws Exception {
		Map metadata = null;
		if (DTOUtils.isProxy(domain) || DTOUtils.isInstance(domain)) {
			metadata = domain.mget();
		} else {
			metadata = domain.getMetadata();
		}
		return buildDataByProvider(metadata, list, MetadataUtils.getDTOMeta(DTOUtils.getDTOClass(domain)));
	}


	public static <T extends IDomain> List<Map> buildDataByProvider(Map medadata, List list) throws Exception {
		return buildDataByProvider(medadata, list, null);
	}


	public static <T extends IDomain> List<Map> buildDataByProvider(Map medadata, List list, ObjectMeta objectMeta) throws Exception {
		if(medadata == null){
			medadata = Maps.newHashMap();
		}
		buildMetadataByObjectMeta(medadata, objectMeta);
		if (medadata.isEmpty()) {
			return list;
		}

		Object extraParam = null;

		if(medadata.containsKey(ValueProvider.EXTRA_PARAMS_KEY)){
			extraParam = medadata.get(ValueProvider.EXTRA_PARAMS_KEY);
			medadata.remove(ValueProvider.EXTRA_PARAMS_KEY);
		}

		Map metadataCopy = new HashMap(medadata.size());
		metadataCopy.putAll(medadata);

		convertStringProvider(metadataCopy);


		List<Map.Entry<String, Object>> metadataCopyList = sortedMetadataCopyList(metadataCopy);


		List<Map> results = new ArrayList<>(list.size());
		for (Object t : list) {
			results.add(BeanConver.transformObjectToMap(t));
		}
		buildResultsByProvider(results, metadataCopyList, objectMeta, extraParam);
		return results;
	}


	private static void buildMetadataByObjectMeta(Map medadata, ObjectMeta objectMeta){
		if(objectMeta != null) {
			for (FieldMeta fieldMeta : objectMeta) {
				if (StringUtils.isNotBlank(fieldMeta.getProvider())) {
					JSONObject meta = new JSONObject();
					meta.put("field", fieldMeta.getName());
					meta.put("index", fieldMeta.getIndex());
					meta.put("provider", fieldMeta.getProvider());
					meta.put("queryParams", fieldMeta.getParams());
					medadata.put(fieldMeta.getName(), meta);
				}
			}
		}
	}


	private static void buildResultsByProvider(List<Map> results, List<Map.Entry<String, Object>> metadataCopyList, ObjectMeta objectMeta, Object extraParam){
		Iterator<Map.Entry<String, Object>> metaCopyIt = metadataCopyList.iterator();
		while(metaCopyIt.hasNext()){
			Map.Entry<String, Object> entry = metaCopyIt.next();
			BatchValueProvider batchValueProvider = null;

			String key = entry.getKey();

			JSONObject jsonValue = null;
			try {
				jsonValue = entry.getValue() instanceof String ? JSONObject.parseObject((String)entry.getValue()) : (JSONObject)JSONObject.toJSON(entry.getValue());
			} catch (JSONException e) {
				continue;
			}

			if(jsonValue.get(ValueProvider.FIELD_KEY) == null){
				jsonValue.put(ValueProvider.FIELD_KEY, key) ;
			}
			if(extraParam != null){
				jsonValue.put(ValueProvider.EXTRA_PARAMS_KEY, extraParam);
			}
			String providerBeanId = jsonValue.get("provider").toString();
			Object bean = SpringUtil.getBean(providerBeanId);
			if (bean instanceof BatchValueProvider) {
				batchValueProvider = (BatchValueProvider) bean;

				try {
					batchValueProvider.setDisplayList(results, jsonValue, objectMeta);
				}catch (Exception e){
					e.printStackTrace();
					LOGGER.error("批量提供者报错:"+batchValueProvider.getClass().getName());
				}
			} else {
				String field = key;
				String childField = null;
				boolean hasChild = false;
				if(field.contains(".")) {
					childField = field.substring(field.indexOf(".") + 1, field.length());
					field = field.substring(0, field.indexOf("."));
					hasChild = true;
				}
				for (Map dataMap : results) {

					String dataKey = key.contains(".") ? key.substring(0, key.indexOf(".")) : key;

					jsonValue.put(ValueProvider.ROW_DATA_KEY, dataMap);
					ValueProvider valueProvider = (ValueProvider) bean;
					FieldMeta fieldMeta = objectMeta == null ? null : objectMeta.getFieldMetaById(dataKey);
					try {
						String text = valueProvider.getDisplayText(dataMap.get(dataKey), jsonValue, fieldMeta);



						if (text != null &&  !(dataMap.get(dataKey) instanceof Date)) {

							if(hasChild){

								String originalKey = new StringBuilder(field).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
								Object fkValueObj = dataMap.get(dataKey);
								if(fkValueObj != null){
									Object fkValue = getObjectValueByKey(fkValueObj, childField);
									if(fkValue != null){
										dataMap.put(originalKey, fkValue);
									}
								}
							}else {
								dataMap.put(ORIGINAL_KEY_PREFIX + field, dataMap.get(dataKey));
							}
						}

						if(text != null && valueProvider instanceof BatchValueProvider) {
							dataMap.put(key, text);

						}else if(!(valueProvider instanceof BatchValueProvider)){
							dataMap.put(key, text);
						}
					}catch (Exception e){
						e.printStackTrace();
						LOGGER.error("提供者报错:"+valueProvider.getClass().getName());
					}
				}
			}
		}
	}


	private static List<Map.Entry<String, Object>> sortedMetadataCopyList(Map metadataCopy){

		List<Map.Entry<String, Object>> metadataCopyList = new ArrayList<Map.Entry<String, Object>>(metadataCopy.entrySet());
		Collections.sort(metadataCopyList, (o1, o2) -> {
			try {
				JSONObject jsonValue1 = o1.getValue() instanceof JSONObject ? (JSONObject) o1.getValue() : JSONObject.parseObject(o1.getValue().toString());
				JSONObject jsonValue2 = o2.getValue() instanceof JSONObject ? (JSONObject) o2.getValue() : JSONObject.parseObject(o2.getValue().toString());
				int index1 = Integer.parseInt(jsonValue1.getOrDefault(ValueProvider.INDEX_KEY, "0").toString());
				int index2 = Integer.parseInt(jsonValue2.getOrDefault(ValueProvider.INDEX_KEY, "0").toString());
				return index1 > index2 ? 1 : index1 < index2 ? -1 : 0;
			} catch (JSONException e) {
				return 0;
			} catch (Exception e){
				return 0;
			}
		});
		return metadataCopyList;
	}


	private static Object getObjectValueByKey(Object obj, String key){
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

	public ValueProvider getProviderObject(String providerId) {
		return valueProviderMap.get(providerId);
	}


	public String getDisplayText(String providerId, Object obj, Map<String, Object> paramMap) {
		ValueProvider providerObj = valueProviderMap.get(providerId);
		return providerObj == null ? "" : providerObj.getDisplayText(obj, paramMap, null);
	}


	public String getDisplayText(FieldMeta fieldMeta, Object theVal, Map<String, Object> paramMap) {
		assert (fieldMeta.getProvider() != null);
		ValueProvider providerObj = valueProviderMap.get(fieldMeta.getProvider());
		return providerObj == null ? "" : providerObj.getDisplayText(theVal, paramMap, fieldMeta);
	}


	public void clearProvider(String providerId) {
		valueProviderMap.remove(providerId);
	}


	public void clearProviders() {
		valueProviderMap.clear();
	}


	public List<ValuePair<?>> getLookupList(String providerId, Object val, Map<String, Object> paramMap) {
		ValueProvider providerObj = valueProviderMap.get(providerId);
		Object queryParamsObj = paramMap.get(ValueProvider.QUERY_PARAMS_KEY);
		String emptyText = ValueProvider.EMPTY_ITEM_TEXT;
		List<ValuePair<?>> valuePairs =  providerObj == null ? Collections.EMPTY_LIST : providerObj.getLookupList(val, paramMap, null);
		if(valuePairs == null) {
			valuePairs = new ArrayList<ValuePair<?>>(1);
		}
		if(queryParamsObj != null){

			JSONObject queryParams = JSONObject.parseObject(queryParamsObj.toString());

			String customEmptyText = queryParams.getString(ValueProvider.EMPTY_ITEM_TEXT_KEY);
			if(customEmptyText != null){
				emptyText = customEmptyText;
			}

			Boolean required = queryParams.getBoolean(ValueProvider.REQUIRED_KEY);

			if(required == null || required.equals(false)){

				if(providerObj != null && !valuePairs.isEmpty() && !"".equals(valuePairs.get(0).getValue())) {
					valuePairs.add(0, new ValuePairImpl<String>(emptyText, ""));
				}
			}
		}else{
			if(providerObj != null && !valuePairs.isEmpty() && !"".equals(valuePairs.get(0).getValue())) {
				valuePairs.add(0, new ValuePairImpl<String>(emptyText, ""));
			}
        }
		return valuePairs;
	}


	@SuppressWarnings("unchecked")
	public List<ValuePair<?>> getLookupList(FieldMeta fieldMeta, Object theVal, Map<String, Object> paramMap) {
		assert (fieldMeta.getProvider() != null);
		ValueProvider providerObj = valueProviderMap.get(fieldMeta.getProvider());
		return providerObj == null ? Collections.EMPTY_LIST : providerObj.getLookupList(theVal, paramMap, fieldMeta);
	}


	private static class ValueProviderFactory {
		protected static final Logger log = LoggerFactory.getLogger(ValueProviderFactory.class);

		private static final Map<Class<? extends ValueProvider>, ValueProvider> BUFFERS = new ConcurrentHashMap<Class<? extends ValueProvider>, ValueProvider>();


		public static ValueProvider getProviderObj(Class<? extends ValueProvider> providerClazz) {
			ValueProvider retval = BUFFERS.get(providerClazz);
			if (retval == null) {
				try {
					retval = providerClazz.newInstance();
					BUFFERS.put(providerClazz, retval);
				} catch (Exception e) {
					log.warn(e.getMessage());
				}
			}
			return retval;
		}
	}



	private static void convertStringProvider(Map<String, Object> medadata){
		if(medadata == null){
			return;
		}
		Iterator<Map.Entry<String, Object>> it = medadata.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Object> entry = it.next();
			if(!isJson(entry.getValue().toString())){
				if(entry.getKey().equals(IDTO.NULL_VALUE_FIELD)
						|| entry.getKey().equals(IDTO.AND_CONDITION_EXPR)
						|| entry.getKey().equals(IDTO.OR_CONDITION_EXPR)
						|| entry.getKey().equals(ValueProvider.EXTRA_PARAMS_KEY)){
					continue;
				}
				Map<String, Object> value = Maps.newHashMap();
				value.put(ValueProvider.PROVIDER_KEY, entry.getValue());
				value.put(ValueProvider.FIELD_KEY, entry.getKey());
				value.put(ValueProvider.INDEX_KEY, 0);

				value.put(ValueProvider.EXTRA_PARAMS_KEY, medadata.get(ValueProvider.EXTRA_PARAMS_KEY));
				entry.setValue(value);
			}
		}
	}

	private static boolean isJson(String content){
		try {
			JSONObject.parseObject(content);
			return  true;
		} catch (Exception e) {
			return false;
		}
	}

}
