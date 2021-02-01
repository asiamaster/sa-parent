package com.sa.datasource.aop;

import com.sa.datasource.SwitchDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DataSourceSwitchAdvice implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceSwitchAdvice.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		before(invocation);
		Object result = null;
		try {
			result = invocation.proceed();
		}finally {
			after(invocation);
		}
		return result;
	}

	private void before(MethodInvocation invocation) {
		Method method = invocation.getMethod();

		if (method.getModifiers() == Modifier.PRIVATE){
			return;
		}
		Class<?> declaringType = null;
		try {

			Field field = ReflectiveMethodInvocation.class.getDeclaredField("targetClass");
			field.setAccessible(true);
			declaringType = (Class)field.get(invocation);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		SwitchDataSource targetDataSource = method.getAnnotation(SwitchDataSource.class);
		targetDataSource = targetDataSource == null ? declaringType.getAnnotation(SwitchDataSource.class) : targetDataSource;
		if (!DynamicRoutingDataSourceContextHolder.containsDataSource(targetDataSource.value())) {

			logger.error("数据源[{}]不存在，使用默认数据源 > {}", targetDataSource.value(), declaringType.getTypeName());
		} else {
			logger.debug("Use DataSource : {} > {}", targetDataSource.value(), declaringType.getTypeName());
			DynamicRoutingDataSourceContextHolder.push(targetDataSource.value());
		}
	}

	private void after(MethodInvocation invocation) {







		if(DynamicRoutingDataSourceContextHolder.getDataSourceType().isEmpty()) {
			DynamicRoutingDataSourceContextHolder.clear();
		}else {
			DynamicRoutingDataSourceContextHolder.pop();
		}
	}

}