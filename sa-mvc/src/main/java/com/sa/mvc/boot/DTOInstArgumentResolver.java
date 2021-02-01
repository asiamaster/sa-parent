package com.sa.mvc.boot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sa.dto.DTO;
import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import com.sa.dto.ReturnTypeHandlerFactory;
import com.sa.exception.AppException;
import com.sa.mvc.annotation.Cent2Yuan;
import com.sa.mvc.servlet.RequestReaderHttpServletRequestWrapper;
import com.sa.mvc.util.BeanValidator;
import com.sa.util.POJOUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletRequest;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.springframework.web.bind.support.WebArgumentResolver.UNRESOLVED;


@SuppressWarnings("all")
public class DTOInstArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return IDTO.class.isAssignableFrom(parameter.getParameterType()) && parameter.getParameterType().isInterface();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Class<?> clazz = parameter.getParameterType();
		if(clazz != null && IDTO.class.isAssignableFrom(clazz)){
			return getDTO((Class<IDTO>)clazz, webRequest, parameter);
		}
		return UNRESOLVED;
	}


	Pattern listObjPattern = Pattern.compile("(\\[)([0-9])+(\\])(\\[)([\\w])+(\\])$");

	
	@SuppressWarnings("unchecked")
	protected <T extends IDTO> T getDTO(Class<T> clazz, NativeWebRequest webRequest, MethodParameter parameter) {







		Map<String, Class<?>> fields = new HashMap<>();
		for(Method method : clazz.getMethods()){


			if("getId".equals(method.getName()) && Serializable.class.equals(method.getReturnType())){
				continue;
			}
			if(POJOUtils.isGetMethod(method) && method.getParameterTypes().length == 0){
				fields.put(POJOUtils.getBeanField(method), method.getReturnType());
			}
		}

		DTO streamDto = getDTO4Restful(fields, webRequest, parameter);
		DTO dto = new DTO();

		for (Map.Entry<String, String[]> entry : webRequest.getParameterMap().entrySet()) {
			String attrName = entry.getKey();

			if(attrName.startsWith("metadata[") && attrName.endsWith("]")){
				dto.setMetadata(attrName.substring(9, attrName.length()-1), getParamValuesAndConvert(entry, fields));
			}else if (Character.isLowerCase(attrName.charAt(0))) {


				if(attrName.endsWith("[]")){
					handleListValue(dto, clazz, attrName, entry.getValue());
				}

				else if(listObjPattern.matcher(attrName).find()){
					handleListObjValue(dto, clazz, attrName, entry.getValue());
				}


				else if(attrName.endsWith("]") && attrName.contains("[")){
					handleMapValue(dto, clazz, attrName, entry.getValue());
				}

				else if(attrName.split("\\.").length == 2){
                    handleDotMapValue(dto, clazz, attrName, entry.getValue());
                }

				else{
					Method method = getMethod(clazz, "get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
					if (method == null) {
						dto.put(attrName, getParamValuesAndConvert(entry, fields));
					} else {

						if(List.class.isAssignableFrom(method.getReturnType()) || method.getReturnType().isArray()){
							Type genericReturnType = method.getGenericReturnType();

							if(genericReturnType instanceof Class){
								dto.put(attrName, Lists.newArrayList(entry.getValue()));
							}else{

								Type retType = ((java.lang.reflect.ParameterizedType)genericReturnType).getActualTypeArguments()[0];

								if(entry.getValue().getClass().isArray()){
									Object[] arrays = (Object[])entry.getValue();
									List objects = new ArrayList(arrays.length);
									if(Long.class.equals(retType)){
										for (Object o : arrays) {
											objects.add(Long.parseLong(o.toString()));
										}
									}else if(Integer.class.equals(retType)){
										for (Object o : arrays) {
											objects.add(Integer.parseInt(o.toString()));
										}
									}else{
										for (Object o : arrays) {
											objects.add(o.toString());
										}
									}
									dto.put(attrName, objects);
								}else{
									dto.put(attrName, ReturnTypeHandlerFactory.convertValue((Class) retType, entry.getValue()));
								}
							}
						}else{
							dto.put(attrName, getParamValuesAndConvert(entry, fields));
						}
					}
				}
			}else{
                dto.put(attrName, getParamValuesAndConvert(entry, fields));
            }
		}


		for (Method method : clazz.getMethods()) {

			if(!method.getName().startsWith("get")){
				continue;
			}
			String field = POJOUtils.getBeanField(method);

			if(method.getReturnType().isArray() && dto.get(field) != null && dto.get(field) instanceof List){
				Class<?> retType = method.getReturnType().getComponentType();

				if(retType == null) {
					dto.put(field, ((List) dto.get(field)).toArray());
				}else{
					Object arr = Array.newInstance(retType, ((List)dto.get(field)).size());
					dto.put(field, ((List) dto.get(field)).toArray((Object[])arr));
				}
			}
		}

		if(!streamDto.isEmpty()) {
			dto.putAll(streamDto);
		}

		if(!streamDto.getMetadata().isEmpty()){
			if(dto.getMetadata() == null){
				dto.setMetadata(streamDto.getMetadata());
			}else {
				dto.getMetadata().putAll(streamDto.getMetadata());
			}
		}

		handleCent2Yuan(clazz, dto);
		T t = (T) DTOUtils.proxyInstance(dto, (Class<IDTO>) clazz);
		asetErrorMsg(t, parameter);
		return t;
	}

	
	private void handleCent2Yuan(Class clazz, DTO dto){
		Method[] methods = clazz.getMethods();
		for(Method method : methods){
			if(!method.getName().startsWith("get") || !Long.class.isAssignableFrom(method.getReturnType())){
				continue;
			}
			Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
			if(cent2Yuan != null){
				String field = POJOUtils.getBeanField(method);
				Object fieldValue = dto.get(field);
				if(fieldValue != null) {
					dto.put(field, yuan2Cent(fieldValue.toString()));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Method getMethod(Class<?> clazz, String methodName){
		try {
			return clazz.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	
	private Long yuan2Cent(String yuan){
		return new BigDecimal(yuan).multiply(new BigDecimal(100)).longValue();
	}

    
	@SuppressWarnings("unchecked")
    private <T extends IDTO> void handleDotMapValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
        String[] names = attrName.split("\\.");
        String attrObjKey = names[1].trim();

        attrName = names[0].trim();

        Method getMethod = null;
        try {
            getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));

            Class<?> returnType = getMethod.getReturnType();

            if(dto.get(attrName) == null){
                if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
                    dto.put(attrName, DTOUtils.newInstance((Class<IDTO>)returnType));
                }else if (!Map.class.isAssignableFrom(returnType)){
                    dto.put(attrName, returnType.newInstance());
                }else{
                    dto.put(attrName, new HashMap<String, Object>());
                }
            }

            if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
                ((IDTO)dto.get(attrName)).aset(attrObjKey, getParamValueByForce(entryValue));
            }else if (!Map.class.isAssignableFrom(returnType)){
                PropertyUtils.setProperty(dto.get(attrName), attrObjKey, getParamValueByForce(entryValue));
            }else{
                ((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
            }
        } catch (NoSuchMethodException e) {

            if(dto.get(attrName) == null) {
                dto.put(attrName, new HashMap<String, Object>());
            }
            ((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {

        }
    }

	
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleMapValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		String attrObjKey = attrName.substring(attrName.lastIndexOf("[")+1, attrName.length()-1);

		attrName = attrName.substring(0, attrName.lastIndexOf("["));

		Method getMethod = null;
		try {
			getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));

			Class<?> returnType = getMethod.getReturnType();

			if(dto.get(attrName) == null){
				if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
					dto.put(attrName, DTOUtils.newInstance((Class<IDTO>)returnType));
				}
				else if(StringUtils.isNumeric(attrObjKey)){
					dto.put(attrName, new ArrayList<>());
				}

				else if (!Map.class.isAssignableFrom(returnType)){
					dto.put(attrName, returnType.newInstance());
				}
				else{
					dto.put(attrName, new HashMap<>());
				}
			}

			if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
				Class<?> fieldType = getFieldType((Class<?>) returnType, attrObjKey);
				if(fieldType == null){
					((IDTO) dto.get(attrName)).aset(attrObjKey, getParamValueByForce(entryValue));
				}else {
					((IDTO) dto.get(attrName)).aset(attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
				}
			}
			else if(StringUtils.isNumeric(attrObjKey)){
				((ArrayList)dto.get(attrName)).add(Integer.parseInt(attrObjKey), getParamValueByForce(entryValue));
			}
			else if (!Map.class.isAssignableFrom(returnType)){
				Class<?> fieldType = getFieldType((Class<?>) returnType, attrObjKey);
				if(fieldType == null){
					PropertyUtils.setProperty(dto.get(attrName), attrObjKey, getParamValueByForce(entryValue));
				}else {
					PropertyUtils.setProperty(dto.get(attrName), attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
				}
			}else{
				((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
			}
		} catch (NoSuchMethodException e) {

			if(dto.get(attrName) == null) {
				dto.put(attrName, new HashMap<String, Object>());
			}
			((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {

		}
	}

	
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleListObjValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		String attrObjKey = attrName.substring(attrName.lastIndexOf("][")+2, attrName.length()-1);
		int index = Integer.valueOf(attrName.substring(attrName.indexOf("[")+1, attrName.lastIndexOf("][")));

		attrName = listObjPattern.matcher(attrName).replaceAll("");

		if(dto.get(attrName) == null){
			dto.put(attrName, new ArrayList<Object>());
		}

		Method getMethod = null;
		try {
			getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));

			Class<?> returnType = getMethod.getReturnType();


			if(List.class.isAssignableFrom(getMethod.getReturnType())
					&& getMethod.getGenericReturnType().getTypeName().endsWith(">")) {
				Type retType = ((java.lang.reflect.ParameterizedType) getMethod.getGenericReturnType()).getActualTypeArguments()[0];

				if ((retType.getClass() instanceof Class<?>)) {

					if (IDTO.class.isAssignableFrom((Class<?>) retType) && ((Class<?>) retType).isInterface()) {
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							IDTO idto = DTOUtils.newInstance((Class<IDTO>) retType);
							list.add(index, idto);
						}
						Class<?> fieldType = getFieldType((Class<?>) retType, attrObjKey);
						if(fieldType == null){
							((IDTO) list.get(index)).aset(attrObjKey, getParamValueByForce(entryValue));
						}else{
							((IDTO) list.get(index)).aset(attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
						}
					}else if(Map.class.isAssignableFrom((Class<?>) retType)){
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Map<String, Object> map = new HashMap<String, Object>();
							list.add(index, map);
						}
						((Map) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
					}else{
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Object obj = ((Class<?>) retType).newInstance();
							list.add(index, obj);
						}
						Class<?> fieldType = getFieldType((Class<?>) retType, attrObjKey);
						if(fieldType == null){
							PropertyUtils.setProperty(list.get(index), attrObjKey, getParamValueByForce(entryValue));
						}else{
							PropertyUtils.setProperty(list.get(index), attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
						}
					}
				}
			}else{
				ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
				if(CollectionUtils.isEmpty(list) || list.size() <= index){
					Map<String, Object> map = new HashMap<String, Object>();
					list.add(index, map);
				}
				((Map<String, Object>) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
			}
		} catch (NoSuchMethodException e) {

			ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
			if(CollectionUtils.isEmpty(list) || list.size() <= index){
				Map<String, Object> map = new HashMap<String, Object>();
				list.add(index, map);
			}
			((Map<String, Object>) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {

		}
	}

	
	private Class<?> getFieldType(Class<?> clazz, String fieldName){
		for(Method method : clazz.getMethods()){

			if(POJOUtils.isGetMethod(method) && method.getParameterTypes().length == 0 && fieldName.equals(POJOUtils.getBeanField(method))){
				return method.getReturnType();
			}
		}
		return null;
	}

	
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleListValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		int index = attrName.lastIndexOf("[");

		attrName = index >= 0 ? attrName.substring(0, index) : attrName;
		try {

			Method getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
			Class<?> returnType = getMethod.getReturnType();


			if(List.class.isAssignableFrom(returnType)){
				Type genericReturnType = getMethod.getGenericReturnType();

				if(genericReturnType instanceof Class){
					paramValue = Lists.newArrayList((Object[])getParamObjValue(entryValue));
				}else{

					Type retType = ((java.lang.reflect.ParameterizedType)getMethod.getGenericReturnType()).getActualTypeArguments()[0];
					Object[] paramObjValue = (Object[]) getParamObjValue(entryValue);
					List objects = new ArrayList(paramObjValue.length);
					if(Long.class.equals(retType)){
						for (Object o : paramObjValue) {
							objects.add(Long.parseLong(o.toString()));
						}
					}else if(Integer.class.equals(retType)){
						for (Object o : paramObjValue) {
							objects.add(Integer.parseInt(o.toString()));
						}
					}else{
						for (Object o : paramObjValue) {
							objects.add(o.toString());
						}
					}
					paramValue = objects;
				}
			}else if(returnType.isArray()){

				Object[] paramObjValue = (Object[]) getParamObjValue(entryValue);

				if(Long.class.equals(returnType.getComponentType())){
					Long[] longs = new Long[paramObjValue.length];
					for (int i = 0; i < paramObjValue.length; i++) {
						longs[i] = Long.parseLong(paramObjValue[i].toString());
					}
					paramValue = longs;
				}
				else if(Integer.class.equals(returnType.getComponentType())){
					Integer[] integers = new Integer[paramObjValue.length];
					for (int i = 0; i < paramObjValue.length; i++) {
						integers[i] = Integer.parseInt(paramObjValue[i].toString());
					}
					paramValue = integers;
				}else{
					paramValue = getParamObjValue(entryValue);
				}
			}else{
				paramValue = getParamObjValue(entryValue);
			}
		} catch (NoSuchMethodException e) {

			paramValue = Lists.newArrayList((Object[])getParamObjValue(entryValue));
		}
		if(paramValue == null) {
			paramValue = getParamValueByForce(entryValue);
		}
		dto.put(attrName, paramValue);
	}

	
	@SuppressWarnings("unchecked")
	private DTO getDTO4Restful(Map<String, Class<?>> fields, NativeWebRequest webRequest, MethodParameter parameter) {

		DTO dto = new DTO();
		try {


			String inputString = webRequest.getNativeRequest() instanceof RequestReaderHttpServletRequestWrapper ? getBodyString((RequestReaderHttpServletRequestWrapper)webRequest.getNativeRequest()) : "";



			if(StringUtils.isNotBlank(inputString)) {
				JSONObject jsonObject = null;
				try {
					inputString = java.net.URLDecoder.decode(inputString, "UTF-8");
				} catch (UnsupportedEncodingException | IllegalArgumentException e) {
				}
				if(JSON.isValid(inputString)) {
					jsonObject = JSONObject.parseObject(inputString);
				}else{
					jsonObject = JSONObject.parseObject(getJsonStrByQueryUrl(inputString));
				}
				for(Map.Entry<String, Object> entry : jsonObject.entrySet()){

					if(entry.getKey().startsWith("metadata[") && entry.getKey().endsWith("]")){
						dto.setMetadata(entry.getKey().substring(9, entry.getKey().length()-1), entry.getValue());
					}
					else if(entry.getKey().equals("metadata") && entry.getValue() instanceof Map){
						dto.setMetadata((Map)entry.getValue());
					}else{
						dto.put(entry.getKey(), getParamValueAndConvert(entry, fields));
					}
				}
			}
			return dto;
		} catch (Exception e) {
			e.printStackTrace();
			return dto;
		}
	}

	
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void asetErrorMsg(T t, MethodParameter parameter){
		Validated validated = parameter.getParameter().getAnnotation(Validated.class);

		if(validated != null) {
			t.aset(IDTO.ERROR_MSG_KEY, BeanValidator.validator(t, validated.value()));
		}
	}

	
	@SuppressWarnings("unchecked")
	private <T extends IDTO> Object convertValue(Map.Entry<String, Object> entry, Class<T> clazz){
		Method getter = null;
		try {
			getter = clazz.getMethod("get"+ entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1));
		} catch (NoSuchMethodException e) {
			try {
				getter = clazz.getMethod("is"+ entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1));
			} catch (NoSuchMethodException e1) {
			}
		}
		if(getter == null){
			return entry.getValue();
		}


		if(List.class.isAssignableFrom(getter.getReturnType())
				&& getter.getGenericReturnType().getTypeName().endsWith(">")){
			Type retType = ((java.lang.reflect.ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0];

			if((retType.getClass() instanceof Class<?>)){

				if(IDTO.class.isAssignableFrom((Class<?>)retType) && ((Class<?>)retType).isInterface()) {
					List<Object> values = (List<Object>) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}
					List<Object> convertedValues = new ArrayList<Object>(values.size());
					values.stream().forEach(v -> {
						if (!(v instanceof Map)) {
							return;
						}
						convertedValues.add(DTOUtils.proxyInstance(new DTO((Map) v), (Class<IDTO>) retType));
					});
					return convertedValues;
				}else{
					List<Object> values = (List) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}

					if(values.get(0).getClass().equals(retType)){
						return values;
					}
                    List<Object> convertedValues = new ArrayList<Object>(values.size());
					values.stream().forEach(v -> {

                        Object convertedValue = ReturnTypeHandlerFactory.convertValue((Class<?>)retType, v);
                        if(convertedValue != null){
                            convertedValues.add(convertedValue);
                        }
					});
					return convertedValues;
				}
			}
		}

		else if(IDTO.class.isAssignableFrom(getter.getReturnType()) && getter.getReturnType().isInterface()){
			Object obj = entry.getValue();
			if(!(obj instanceof Map)) {
				return obj;
			}
			return DTOUtils.proxyInstance(new DTO((Map)obj), (Class<IDTO>) getter.getReturnType());
		}
		return entry.getValue();
	}


	
	@SuppressWarnings("unchecked")
	private Object getParamValuesAndConvert(Map.Entry<String, String[]> entry, Map<String, Class<?>> fields) {
		if(entry == null || entry.getValue() == null){
			return null;
		}
		String val = getParamValue(entry.getValue());
		if(StringUtils.isEmpty(val)){
			val = null;
		}
		if(fields.containsKey(entry.getKey())){
			if(String.class.equals(fields.get(entry.getKey()))){
				return val;
			}
			return ReturnTypeHandlerFactory.convertValue(fields.get(entry.getKey()), val);
		}
		return val == null ? null : StringUtils.isBlank(val) ? null : val;
	}

	private Object getParamValueAndConvert(Map.Entry<String, Object> entry, Map<String, Class<?>> fields) {
		if(entry == null || entry.getValue() == null){
			return null;
		}
		Object val = entry.getValue();
		if(fields.containsKey(entry.getKey())){
			if(String.class.equals(fields.get(entry.getKey()))){
				return val.toString();
			}
			return ReturnTypeHandlerFactory.convertValue(fields.get(entry.getKey()), val);
		}
		return val == null ? null : val;
	}

	
	@SuppressWarnings("unchecked")
	private String getParamValueByForce(Object obj) {
		String val = getParamValue(obj);
		return val == null ? null : StringUtils.isBlank(val) ? null : val;
	}

	
	@SuppressWarnings("unchecked")
	private Object getParamObjValue(Object obj) {
		return obj == null ? null : obj.getClass().isArray() ? java.io.File.class.isAssignableFrom(((Object[]) obj)[0].getClass()) ? null  : obj : obj;
	}

	
	@SuppressWarnings("unchecked")
	private String getParamValue(Object obj) {
		return (String) (obj == null ? null : obj.getClass().isArray() ? java.io.File.class.isAssignableFrom(((Object[]) obj)[0].getClass()) ? null  : ((Object[]) obj)[0] : obj);
	}

	final static int BUFFER_SIZE = 4096;
	
	@SuppressWarnings("unchecked")
	private String InputStream2String(InputStream in, String encoding) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		bufferedInputStream.mark(0);
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while((count = bufferedInputStream.read(data,0,BUFFER_SIZE)) != -1) {
			outStream.write(data, 0, count);
		}
		bufferedInputStream.reset();
		data = null;
		return new String(outStream.toByteArray(), encoding);
	}

	
	private String getBodyString(ServletRequest request) {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = request.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			throw new AppException("获取requestBody出错：" + e.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignored) {
			}
		}
		return sb.toString();
	}

	
	public static String getJsonStrByQueryUrl(String paramStr){

		String[] params = paramStr.split("&");
		JSONObject obj = new JSONObject();
		for (int i = 0; i < params.length; i++) {
			String[] param = params[i].split("=");
			if (param.length >= 2) {
				String key = param[0];
				String value = param[1];
				for (int j = 2; j < param.length; j++) {
					value += "=" + param[j];
				}
				try {
					obj.put(key,value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return obj.toString();
	}
}