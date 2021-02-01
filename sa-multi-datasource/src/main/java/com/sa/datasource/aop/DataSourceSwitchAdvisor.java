package com.sa.datasource.aop;

import com.sa.datasource.DataSourceManager;
import com.sa.datasource.SwitchDataSource;
import com.sa.datasource.SwitchMode;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@ConditionalOnExpression("'${spring.datasource.switch-mode}'=='1' || '${spring.datasource.switch-mode}'=='2'")
public class DataSourceSwitchAdvisor extends DefaultPointcutAdvisor {

	private static final long serialVersionUID = -3783115445256706953L;

	@Override
	public Pointcut getPointcut() {

		if(DataSourceManager.switchMode.equals(SwitchMode.MASTER_SLAVE)){
			ComposablePointcut pc=new ComposablePointcut(new AnnotationMatchingPointcut(SwitchDataSource.class, true));
			pc.union(AnnotationMatchingPointcut.forMethodAnnotation(SwitchDataSource.class));
			pc.union(new AnnotationMatchingPointcut(Transactional.class, true));
			pc.union(AnnotationMatchingPointcut.forMethodAnnotation(Transactional.class));
			return pc;

		} else if (DataSourceManager.switchMode.equals(SwitchMode.MULTI)){
			ComposablePointcut pc=new ComposablePointcut(new AnnotationMatchingPointcut(SwitchDataSource.class, true));
			pc.union(AnnotationMatchingPointcut.forMethodAnnotation(SwitchDataSource.class));
			return pc;
		} else {
			return null;
		}
	}

	@Override
	public Advice getAdvice() {
		if(DataSourceManager.switchMode.equals(SwitchMode.MASTER_SLAVE)){
			return new MasterSlaveDataSourceAdvice();
		}else if (DataSourceManager.switchMode.equals(SwitchMode.MULTI)){
			return new DataSourceSwitchAdvice();
		}else{
			return null;
		}
	}

}
