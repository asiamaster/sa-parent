package com.sa.datasource.aop;

import com.sa.datasource.SwitchDataSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class DynamicRoutingDataSourceAspect {

	private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSourceAspect.class);









	public void withinTargetDataSource() {
	}



	public void before(JoinPoint point) {








	}



	public Object around(ProceedingJoinPoint point) throws Throwable {
		System.out.println("around:"+point.getTarget());
		Object result = point.proceed();
		return result;
	}

	public void restoreDataSource(JoinPoint point, SwitchDataSource ds) {
		logger.debug("Revert DataSource : {} > {}", ds.value(), point.getSignature());
		DynamicRoutingDataSourceContextHolder.clear();
	}
}
