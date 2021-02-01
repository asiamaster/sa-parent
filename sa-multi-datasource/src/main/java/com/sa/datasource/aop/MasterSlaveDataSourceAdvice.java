package com.sa.datasource.aop;

import com.sa.datasource.DataSourceType;
import com.sa.datasource.SwitchDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class MasterSlaveDataSourceAdvice implements MethodInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MasterSlaveDataSourceAdvice.class);

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

		if (method.getModifiers() == Modifier.PRIVATE)
			return;

		Method stackTopMethod = MasterSlaveContextHolder.getStackTopMethod();

		if(stackTopMethod == null){


			stackTopMethod = method;
			MasterSlaveContextHolder.pushOuterMethod(method);
		}else{

			if(!MasterSlaveContextHolder.writable()){
				MasterSlaveContextHolder.pushOuterMethodByForce(method);
			}
		}

		if(!MasterSlaveContextHolder.writable()){
			boolean writable = false;

			Class<?> declaringType = stackTopMethod.getDeclaringClass();

			boolean transactionalPresent = stackTopMethod.isAnnotationPresent(Transactional.class) || declaringType.isAnnotationPresent(Transactional.class);

			SwitchDataSource dataSourceSwitchMethod = stackTopMethod.getAnnotation(SwitchDataSource.class);

			SwitchDataSource dataSourceSwitch = dataSourceSwitchMethod == null ? declaringType.getAnnotation(SwitchDataSource.class): dataSourceSwitchMethod;
			writable = transactionalPresent ? true : dataSourceSwitch == null ? false : DataSourceType.MASTER == dataSourceSwitch.type();
			determinDataSource(writable);
		}
	}

	private void after(MethodInvocation invocation) {



		MasterSlaveContextHolder.clean();
	}

	private void determinDataSource(boolean writable) {


		if (MasterSlaveContextHolder.writable())
			return;

		if (writable)
			MasterSlaveContextHolder.write();
		else
			MasterSlaveContextHolder.read();

		LOGGER.info("according to dataSource switcher, try switch to {} dataSource", writable ? "writable" : "read only");
	}

}