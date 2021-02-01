package com.sa.dto;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sa.metadata.annotation.FieldDef;
import com.sa.util.POJOUtils;
import com.sa.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Function;


public class DTOHandler<T extends DTO> implements InvocationHandler, Serializable {
	private static final long serialVersionUID = -7340937653355927470L;

	private Class<?> proxyClazz;

	private T delegate;

	
	public DTOHandler(Class<?> proxyClazz, T delegate) {
		this.proxyClazz = proxyClazz;
		this.delegate = delegate;
	}

	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (POJOUtils.isBeanMethod(method)) {
			String field = POJOUtils.getBeanField(method);
			Object retval = null;

			if (POJOUtils.isSetMethod(method)) {
				assert (args != null);
				assert (args.length > 0);
				FieldDef fieldDef = method.getAnnotation(FieldDef.class);
				if(fieldDef == null || fieldDef.handler() == Function.class){
					delegate.put(field, args[0]);
				}else{
					delegate.put(field, fieldDef.handler().newInstance().apply(args[0]));
				}


			} else {

				Class<?> returnType = method.getReturnType();
				FieldDef fieldDef = method.getAnnotation(FieldDef.class);

				if(fieldDef != null && !fieldDef.handler().isInterface()) {
					return fieldDef.handler().newInstance().apply(delegate.get(field));
				}

				if (delegate.containsKey(field)) {
					retval = delegate.get(field);


					if (returnType.isPrimitive()) {

						if (retval == null) {
							return POJOUtils.getPrimitiveDefault(returnType);

						} else if (!returnType.equals(retval.getClass())) {
							return POJOUtils.getPrimitiveValue(returnType, retval);
						} else{
							return retval;
						}
					}
					else if (retval == null){
						return null;
					}
					else if(String.class.equals(returnType)){
						return retval.toString();
					}
					else if(returnType.isAssignableFrom(DTOUtils.getDTOClass(retval))){
						return retval;
					}
					else if(StringUtils.isBlank(retval.toString())){
						return null;
					}


					Object convertedValue = ReturnTypeHandlerFactory.convertValue(returnType, retval);
					if(convertedValue != null){
						delegate.put(field, convertedValue);
						return convertedValue;
					}


					if(IDTO.class.isAssignableFrom(DTOUtils.getDTOClass(retval)) && IDTO.class.isAssignableFrom(returnType)){
						JSONObject jo = ((JSONObject) Proxy.getInvocationHandler(retval));
						retval = DTOUtils.as(new DTO(jo), (Class<IDTO>)returnType);
					}

					else if (returnType.isEnum() && retval instanceof String) {
						retval = Enum.valueOf((Class<? extends Enum>) returnType, (String) retval);
					}
					delegate.put(field, retval);

				}

				else if(method.isDefault()){
					return ReflectionUtils.invokeDefaultMethod(proxy, method, null);
				}

				else if (returnType.isPrimitive()) {
					return POJOUtils.getPrimitiveDefault(returnType);
				}
			}
			return retval;

		} else if ("aget".equals(method.getName())) {
			return args == null ? delegate : delegate.get(args[0]);
		} else if ("aset".equals(method.getName())) {
			if(args.length == 1 && args[0] instanceof DTO){
				delegate.putAll((DTO)args[0]);
				return null;
			}else{
				return delegate.put(((String) args[0]), args[1]);
			}
		} else if ("mget".equals(method.getName())) {
			if(args == null) {
				return delegate.getMetadata();
			}else {
				return delegate.getMetadata((String)args[0]);
			}
		} else if ("mset".equals(method.getName())) {
			if(args.length == 1 && args[0] instanceof Map){
				delegate.getMetadata().putAll((Map) args[0]);
				return null;
			}else {
				return delegate.setMetadata(((String) args[0]), args[1]);
			}
		} else if ("toString".equals(method.getName()) && args == null) {
			String data = delegate == null ? "" : JSON.toJSONString(delegate);
			String meta = JSON.toJSONString(delegate.getMetadata());
			StringBuilder stringBuilder = new StringBuilder(proxyClazz.getName());
			stringBuilder.append("\r\ndata:").append(data).append("\r\nmeta:").append(meta);
			return stringBuilder.toString();
		}else {
			return method.invoke(delegate, args);
		}
	}

	public static String getClobString(java.sql.Clob c) {
		try {
			Reader reader=c.getCharacterStream();
			if (reader == null) {
				return null;
			}
			StringBuffer sb = new StringBuffer();
			char[] charbuf = new char[4096];
			for (int i = reader.read(charbuf); i > 0; i = reader.read(charbuf)) {
				sb.append(charbuf, 0, i);
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	
	public Class<?> getProxyClazz() {
		return proxyClazz;
	}

	
	T getDelegate() {
		return delegate;
	}

	
	public Map<String, Object> getMetadata() {
		return delegate.getMetadata();
	}

	void setProxyClazz(Class<?> proxyClazz) {
		this.proxyClazz = proxyClazz;
	}




























































}