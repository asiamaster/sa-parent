package com.sa.dto;

import com.sa.domain.BaseDomain;
import com.sa.exception.InternalException;
import com.sa.exception.ParamErrorException;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.MetadataUtils;
import com.sa.metadata.ObjectMeta;
import com.sa.util.BeanConver;
import com.sa.util.CloneUtils;
import com.sa.util.POJOUtils;
import com.sa.util.ReflectionUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;


public class DTOUtils {

	protected static final Logger logger = LoggerFactory
			.getLogger(DTOUtils.class);

	private static final String INVALID_DELEGATE_ERROR = "类型不符合要求, 转换DTO的代理对象出错！";
	private static final String CREATE_PROXY_ERROR = "要创建代理的代理类({0})不是接口或者没有表示代理的接口！";
	private static final String TRANS_PROXY_ERROR = "转换DTO的代理对象出错！";

	private static final String TO_ENTITY_ERROR = "类为{0}的DTO对象转实体{1}出错!";
	public static Map<String,BeanCopier> beanCopierMap = new HashMap<String, BeanCopier>();


	public static DTO go(Object obj) {
		return go(obj, false);
	}


	public static DTO go(Object obj, boolean withDef) {
		if (obj == null) {
			return null;
		} else if (obj instanceof DTO) {
			return (DTO) obj;
		} else if (isProxy(obj)) {
			DTOHandler handler = (DTOHandler) Proxy.getInvocationHandler(obj);
			if(withDef) {
				DTO dto = new DTO();
				Class<?> clazz = handler.getProxyClazz();
				for (Method method : clazz.getMethods()) {
					if (method.isDefault() && POJOUtils.isGetMethod(method)) {
						Object result = null;
						try {
							result = ReflectionUtils.invokeDefaultMethod(obj, method, null);
						} catch (Throwable throwable) {
							throwable.printStackTrace();
							return null;
						}
						String field = POJOUtils.getBeanField(method);
						dto.put(field, result);
					}
				}
				dto.putAll(handler.getDelegate());
				return dto;
			}else{
				return handler.getDelegate();
			}
		}else if(obj.getClass().getName().endsWith(DTOInstance.SUFFIX)){
			try {
				if(withDef) {
					DTO dto = new DTO();
					for (Method method : getDTOClass(obj).getMethods()) {
						if (method.isDefault() && POJOUtils.isGetMethod(method)) {
							Object result = ReflectionUtils.invokeDefaultMethod(obj, method, null);
							String field = POJOUtils.getBeanField(method);
							dto.put(field, result);
						}
					}
					dto.putAll(((IDTO) obj).aget());
					dto.putAll(transformBeanToMap(obj));
					return dto;
				}else{
					DTO dto = ((IDTO)obj).aget();
					dto.putAll(transformBeanToMap(obj));
					return dto;
				}
			} catch (Exception e) {

			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
		return null;
	}


	public static <T extends IDTO> T clone(T obj, Class<T> proxyClazz){
		assert (obj != null);
		DTO dto = go(obj);
		if(dto == null){
			return null;
		}
		return DTOUtils.proxy(CloneUtils.clone(dto), proxyClazz);
	}


	public final static Class<?> getDTOClass(Object dto) {
		assert (dto != null);
		if (Proxy.isProxyClass(dto.getClass())) {
			InvocationHandler handler = Proxy.getInvocationHandler(dto);
			if (handler instanceof DTOHandler) {
				return ((DTOHandler<?>) handler).getProxyClazz();
			}

			else if("com.alibaba.fastjson.JSONObject".equals(handler.getClass().getName())){
				return IDTO.class;
			} else {
				throw new InternalException("当前代理对象不是DTOHandler能处理的对象!");
			}
		} else if(isInstance(dto)){
			return dto.getClass().getInterfaces()[0];
		} else{
			return dto.getClass();
		}
	}


	public  final static Class<?> getInstanceClass(Class<? extends IDTO> proxyClz) {
		return DTOInstance.cache.get(proxyClz);
	}


	public static boolean isEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		Object id1 = getId(o1);
		Object id2 = getId(o2);
		return id1 == null ? false : id1.equals(id2);
	}


	public static Object getId(Object object) {
		return getProperty(object, IBaseDomain.ID);
	}


	public static boolean isProxy(Object object) {
		assert (object != null);
		return internalIsProxy(object, DTOHandler.class);
	}


	public static <T extends IDTO> T proxy(DTO realObj, Class<T> proxyClz) {
		return internalProxy(realObj, proxyClz, DTOHandler.class);
	}


	public static <T extends IDTO> T proxyInstance(DTO realObj, Class<T> proxyClz) {
		T retval = null;

		if (proxyClz.isInterface()) {
			retval = BeanConver.copyMap(realObj, proxyClz);
			retval.aset(realObj);
		}else{
			throw new ParamErrorException("proxyClz参数必须是实现IDTO的接口类");
		}

		generateDefaultValue(realObj, proxyClz);
		return retval;
	}


	public static <T extends IDTO> T newDTO(Class<T> dtoClz) {
		return proxy(new DTO(), dtoClz);
	}


	public static <T extends IDTO> T newInstance(Class<T> dtoClz) {
		return newInstance(dtoClz, true);
	}


	public static <T extends IDTO> T newInstance(Class<T> dtoClz, boolean genDef) {
		Class<? extends IDTO> clazz = DTOInstance.cache.get(dtoClz);
		try {
			if(clazz == null){
				return newDTO(dtoClz);
			}
			T t = (T) clazz.newInstance();

			if(genDef) {
				generateDefaultValue(t.aget(), dtoClz);
			}
			return t;
		} catch (InstantiationException e) {
			return newDTO(dtoClz);
		} catch (IllegalAccessException e) {
			return newDTO(dtoClz);
		}
	}


	public static <T extends IDTO> List<T> as(List sources, Class<T> proxyClz) {
		assert (sources != null);
		assert (proxyClz != null);
		List<T> list = new ArrayList<T>(sources.size());
		for (Object source : sources) {
			list.add(internalAs(source, proxyClz));
		}
		return list;
	}


	public static <T extends IDTO> List<T> asInstance(List sources, Class<T> proxyClz) {
		assert (sources != null);
		assert (proxyClz != null);
		List<T> list = new ArrayList<T>(sources.size());
		for (Object source : sources) {
			list.add(internalAsInstance(source, proxyClz));
		}
		return list;
	}


	@SuppressWarnings("unchecked")
	public static <T extends IDTO> T as(Object source, Class<T> proxyClz) {
		assert (source != null);
		assert (proxyClz != null);
		return internalAs(source, proxyClz);
	}


	@SuppressWarnings("unchecked")
	public static <T extends IDTO> T asInstance(Object source, Class<T> proxyClz) {
		assert (source != null);
		assert (proxyClz != null);
		return internalAsInstance(source, proxyClz);
	}



	public static<T extends IDTO,K extends IDTO> K bean2Instance(T source, Class<K> target ){
		if (source == null) {
			return null;
		}
		K result = (K)DTOUtils.newInstance(target);
		org.springframework.beans.BeanUtils.copyProperties(source, result);
		return result;
	}


	public static <T extends IDTO> List<T> switchEntityListToDTOList(List<BaseDomain> sources, Class<T> proxyClz) {
		assert (sources != null);
		assert (proxyClz != null);
		List<T> list = new ArrayList<T>(sources.size());
		for (BaseDomain source : sources) {
			list.add(switchEntityToDTO(source, proxyClz));
		}
		return list;
	}


	public static <T extends IDTO> T switchEntityToDTO(BaseDomain source, Class<T> proxyClz) {
		if(source==null||proxyClz==null) {
			return null;
		}
		T temp = DTOUtils.newDTO(proxyClz);
		try {
			org.springframework.beans.BeanUtils.copyProperties(source, temp);
		}catch(Exception e) {
			e.printStackTrace(System.err);
		}
		return temp;
	}


	public static <T extends IDTO> List<T> proxy(List<? extends DTO> realList, Class<T> proxyClz) {
		assert (proxyClz != null);

		if (realList == null) {
			return Collections.EMPTY_LIST;
		}


		return new DTOList<T>(proxyClz, realList);
	}


	public static boolean isInstance(Object object) {
		return object.getClass().getName().endsWith(DTOInstance.SUFFIX);
	}


	public static <M, N extends BaseDomain> N toEntity(M sourceDTO, Class<N> entityClazz, boolean enhance) {
		assert (sourceDTO != null);
		assert (DTOUtils.isInstance(sourceDTO));
		assert (entityClazz != null);
		try {
			if(enhance) {
				return BeanConver.copyMap(go(sourceDTO), entityClazz);
			}else {
				return BeanConver.copyBean(sourceDTO, entityClazz);
			}
		} catch (Exception e) {
			String message = MessageFormat.format(TO_ENTITY_ERROR, sourceDTO.getClass().getName(), entityClazz.getName());
			logger.error(message, e);
		}
		return null;
	}


	public static <T extends IDTO> void decodeDTO2UTF8(T dto) throws UnsupportedEncodingException {
		ObjectMeta om = MetadataUtils.getDTOMeta(DTOUtils.getDTOClass(dto));
		for(FieldMeta fm : om){
			if(String.class.isAssignableFrom(fm.getType())){
				DTO dd = DTOUtils.go(dto);
				if(dd == null || dd.isEmpty()) {
					return;
				}
				if(dd.get(fm.getName()) != null) {
					dd.put(fm.getName(), new String(dd.get(fm.getName()).toString().getBytes("ISO8859-1"), "UTF-8"));
				}
			}
		}
	}


	public static <T extends IDTO> T link(T master, IDTO second, Class<T> masterClazz) {
		return link(master, second, masterClazz, false);
	}


    public static <T extends IDTO> T link(T master, IDTO second, Class<T> masterClazz, boolean isInstance) {
        if (second == null) {
            return master;
        }
        if (master == null) {
            return as(second, masterClazz);
        }
        DTO temp = go(second);
        temp.putAll(go(master));
        return isInstance ? asInstance(second, masterClazz) : as(second, masterClazz);
    }




	final static Object getProperty(Object object, String name) {
		if(isProxy(object)){
			return POJOUtils.getProperty(go(object), name);
		}
		return POJOUtils.getProperty(object, name);
	}


	final static Object setProperty(Object object, String name,
	                                Object value) {
		POJOUtils.setProperty(object, name, value);
		return object;
	}


	@SuppressWarnings("unchecked")
	final static <T extends IDTO> T internalProxy(DTO realObj,
	                                              Class<T> proxyClz, Class<? extends DTOHandler> handlerClazz) {
		T retval = null;

		if (proxyClz.isInterface()) {
			retval = (T) Proxy.newProxyInstance(proxyClz.getClassLoader(),
					new Class<?>[] { proxyClz }, newDTOHandler(
							handlerClazz, proxyClz, realObj));
		} else {

			Class<?>[] interfaces = proxyClz.getInterfaces();
			if (interfaces != null) {
				retval = (T) Proxy.newProxyInstance(proxyClz.getClassLoader(),
						interfaces, newDTOHandler(handlerClazz, proxyClz,
								realObj));
			} else {
				String message = MessageFormat.format(CREATE_PROXY_ERROR,
						proxyClz.getName());
				logger.warn(message);
				throw new DTOProxyException(message);
			}
		}

		generateDefaultValue(realObj, proxyClz);
		return retval;
	}


	@SuppressWarnings("unchecked")
	final static <T extends IDTO> T internalAs(Object source,
	                                           Class<T> proxyClz) {
		assert (source != null);
		assert (proxyClz != null);

		if (source instanceof DTO) {
			return internalProxy((DTO) source, proxyClz, DTOHandler.class);
		} else if (proxyClz.isAssignableFrom(source.getClass())) {
			return (T) source;
		} else if (internalIsProxy(source, DTOHandler.class)) {
			try {
				DTOHandler handler = (DTOHandler) Proxy
						.getInvocationHandler(source);
				return proxy(handler, proxyClz);
			} catch (Exception ex) {
				logger.warn(TRANS_PROXY_ERROR);
				throw new DTOProxyException(TRANS_PROXY_ERROR);
			}
		} else if( source instanceof BaseDomain){
			return switchEntityToDTO((BaseDomain)source, proxyClz);
		} else {
			DTO dto = new DTO();
			Method[] methods = source.getClass().getMethods();
			for(Method method : methods){

				if(POJOUtils.isGetMethod(method) && method.getParameters().length == 0){
					String fieldName = POJOUtils.getBeanField(method);
					try {
						dto.put(fieldName, method.invoke(source));
					} catch (Exception ex) {



					}

				}
			}
			return proxy(dto, proxyClz);
		}


	}


	@SuppressWarnings("unchecked")
	final static <T extends IDTO> T internalAsInstance(Object source,
											   Class<T> proxyClz) {
		assert (source != null);
		assert (proxyClz != null);

		if (source instanceof DTO) {
			T instance = BeanConver.copyMap((DTO)source, proxyClz);;
			try {
				instance.aset((DTO)source);

			} catch (Exception e) {

			}
			return instance;
		} else if (proxyClz.isAssignableFrom(source.getClass())) {
			return (T) source;
		} else if (internalIsProxy(source, DTOHandler.class)) {
			DTOHandler handler = (DTOHandler) Proxy
					.getInvocationHandler(source);
			IDTO instance = bean2Instance((IDTO)source, proxyClz);
			try {
				instance.aset(handler.getDelegate());
			} catch (Exception e) {

			}
			return (T)instance;
		} else if (isInstance(source)) {
			T instance = null;
			try {

				instance = (T)BeanConver.copyBean(source, DTOUtils.getInstanceClass(proxyClz));

				instance.aset(((IDTO)source).aget());
			} catch (Exception e) {

			}
			return (T)instance;
		}else if( source instanceof BaseDomain){

			return bean2Instance((IDTO)source, proxyClz);
		} else if( source instanceof Map) {
			T instance = DTOUtils.newInstance(proxyClz);
			Map map = (Map)source;
			Method[] methods = proxyClz.getMethods();
			DTO dto = new DTO();
			dto.putAll(map);
			try {
				instance.getClass().getMethod("aset", DTO.class).invoke(instance, dto);
			} catch (Exception e) {

			}
			for(Method method : methods){

				if(POJOUtils.isSetMethod(method) && method.getParameters().length == 1){
					String fieldName = POJOUtils.getBeanField(method);
					try {
						if(map.containsKey(fieldName)) {
							method.invoke(instance, map.get(fieldName));
						}
					} catch (Exception ex) {



					}
				}

			}
			return instance;
		}else{




			return (T)BeanConver.copyBean(source, DTOUtils.getInstanceClass(proxyClz));
		}


	}


	@SuppressWarnings("unchecked")
	final static boolean internalIsProxy(Object object,
	                                     Class<? extends DTOHandler> handlerClazz) {
		assert (object != null);
		assert (handlerClazz != null);

		if (Proxy.isProxyClass(object.getClass())) {
			try {
				InvocationHandler handler = Proxy.getInvocationHandler(object);
				return handlerClazz.isAssignableFrom(handler.getClass());
			} catch (Exception ex) {
				return false;
			}
		}
		return false;
	}


	@SuppressWarnings("unchecked")
	final static <T extends IDTO> T proxy(DTOHandler handler,
	                                      Class<T> proxyClz) {
		T retval = null;

		if (proxyClz.isInterface()) {
			retval = (T) Proxy.newProxyInstance(proxyClz.getClassLoader(),
					new Class<?>[] { proxyClz }, handler);

		} else {
			Class<?>[] interfaces = proxyClz.getInterfaces();
			if (interfaces != null) {
				retval = (T) Proxy.newProxyInstance(proxyClz.getClassLoader(),
						interfaces, handler);
			} else {
				String message = MessageFormat.format(CREATE_PROXY_ERROR,
						proxyClz.getName());
				logger.warn(message);
				throw new DTOProxyException(message);
			}
		}

		generateDefaultValue(handler.getDelegate(), proxyClz);

		handler.setProxyClazz(proxyClz);
		return retval;
	}


	@SuppressWarnings("unchecked")
	final static DTOHandler newDTOHandler(
			Class<? extends DTOHandler> handlerClazz, Class proxyClazz,
			Object realObj) {
		try {
			Constructor<? extends DTOHandler> method = null;
			if (DTOHandler.class.isAssignableFrom(handlerClazz)) {
				method = handlerClazz.getConstructor(new Class[] { Class.class,
						DTO.class });
			}
			else {
				method = handlerClazz.getConstructor(new Class[] { Class.class,
						realObj.getClass() });
			}
			return method.newInstance(new Object[] { proxyClazz, realObj });
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new DTOProxyException(CREATE_PROXY_ERROR, e);
		}
	}


	@SuppressWarnings("unchecked")
	static void generateDefaultValue(DTO dtoData, Class<?> proxyClz) {
		ObjectMeta objectMeta = MetadataUtils.getDTOMeta(proxyClz);
		for (FieldMeta fieldMeta : objectMeta) {

			if (dtoData.containsKey(fieldMeta.getName())) {
				continue;
			}

			String defStr = fieldMeta.getDefValue();
			Class<?> type = fieldMeta.getType();


			if (StringUtils.isNotBlank(defStr)) {
				if (type.isEnum()) {
					try {
						dtoData.put(fieldMeta.getName(), Enum.valueOf(
								(Class<? extends Enum>) type, defStr));
					} catch (RuntimeException e) {
						logger.warn("设置默认值时出错：", e);
					}
				} else if (type.isPrimitive()) {
					dtoData.put(fieldMeta.getName(), POJOUtils
							.getPrimitiveValue(type, defStr));
				} else if (Date.class.isAssignableFrom(type)) {
					try {
						dtoData.put(fieldMeta.getName(), DateUtils.parseDate(defStr, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"));
					} catch (ParseException e) {
						e.printStackTrace();
						logger.warn("设置默认值时出错：", e);
					}
				} else {
					try {
						dtoData.put(fieldMeta.getName(), ConvertUtils.convert(
								defStr, type));
					} catch (Exception e) {
						logger.warn("设置默认值时出错：", e);
					}
				}




			}
		}
	}


	private static Map<String, Object> transformBeanToMap(Object bean) throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
		Map<String, Object> returnMap = new HashMap<String, Object>();
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (!"class".equals(propertyName) && !"fields".equals(propertyName)) {
				Method readMethod = descriptor.getReadMethod();

				if(readMethod == null){
					continue;
				}
				Object result = null;
				try {
					result = readMethod.invoke(bean, new Object[0]);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
				if (result != null) {
					returnMap.put(propertyName, result);
				}
			}
		}
		return returnMap;
	}

}