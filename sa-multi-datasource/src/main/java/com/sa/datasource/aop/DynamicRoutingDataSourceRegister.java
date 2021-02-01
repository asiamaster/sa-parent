package com.sa.datasource.aop;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.sa.constant.SsConstants;
import com.sa.datasource.*;
import com.sa.datasource.selector.RoundRobinSelector;
import com.sa.datasource.selector.WeightedRoundRobinSelector;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;






public class DynamicRoutingDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSourceRegister.class);



	private static final Object DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";


	private DataSource defaultDataSource;
	private Map<String, DataSource> customDataSources = new HashMap<>();

	@Autowired
	PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();

		targetDataSources.put("dataSource", defaultDataSource);
		DynamicRoutingDataSourceContextHolder.dataSourceIds.add(SwitchDataSource.DEFAULT_DATASOURCE);

		targetDataSources.putAll(customDataSources);
		for (String key : customDataSources.keySet()) {
			DynamicRoutingDataSourceContextHolder.dataSourceIds.add(key);
		}

		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		if(DataSourceManager.switchMode.equals(SwitchMode.MULTI)) {
			beanDefinition.setBeanClass(DynamicRoutingDataSource.class);
		}else {
			beanDefinition.setBeanClass(DynamicSelectorDataSource.class);
		}
		beanDefinition.setSynthetic(true);
		MutablePropertyValues mpv = beanDefinition.getPropertyValues();
		mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mpv.addPropertyValue("targetDataSources", targetDataSources);
		if(DataSourceManager.switchMode.equals(SwitchMode.MASTER_SLAVE)) {
			if(DataSourceManager.selectorMode.equals(SelectorMode.ROUND_ROBIN)) {
				mpv.addPropertyValue("dataSourceSelector", new RoundRobinSelector());
			}else{
				mpv.addPropertyValue("dataSourceSelector", new WeightedRoundRobinSelector());
			}
		}
		registry.registerBeanDefinition("dataSource", beanDefinition);
		logger.info("DynamicRoutingDataSource Registry");
	}

	
	@Override
	public void setEnvironment(Environment env) {
		initDefaultDataSource(env);
		initCustomDataSources(env);
	}

	
	private void initDefaultDataSource(Environment env) {


		Map<String, Object> dsMap = new HashMap<>();

		DataSourceManager.switchMode = SwitchMode.getSwitchModeByCode(env.getProperty("spring.datasource.switch-mode", "1"));
		if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)) {

			DataSourceManager.selectorMode = SelectorMode.getSelectorModeByCode(env.getProperty("spring.datasource.selector-mode", "1"));
		}
		dsMap.put("type", env.getProperty("spring.datasource.type"));
		dsMap.put("driver-class-name", env.getProperty("spring.datasource.driver-class-name"));
		dsMap.put("url", env.getProperty("spring.datasource.url"));
		dsMap.put("username", env.getProperty("spring.datasource.username"));
		dsMap.put("password", env.getProperty("spring.datasource.password"));
		defaultDataSource = buildDataSource(dsMap, env);
	}

	
	private void initCustomDataSources(Environment env) {


		String dsPrefixs = env.getProperty("spring.datasource.names");
		for (String dsPrefix : dsPrefixs.split(",")) {
			Map<String, Object> dsMap = new HashMap<>();
			dsMap.put("driver-class-name", env.getProperty("spring.datasource."+dsPrefix+".driver-class-name"));
			dsMap.put("url", env.getProperty("spring.datasource."+dsPrefix+".url"));
			dsMap.put("username", env.getProperty("spring.datasource."+dsPrefix+".username"));
			dsMap.put("password", env.getProperty("spring.datasource."+dsPrefix+".password"));
			DataSource ds = buildDataSource(dsMap, env);
			customDataSources.put(dsPrefix, ds);

			if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)){
				DataSourceManager.slaves.add(dsPrefix);
				Object weightObj = dsMap.get("weight");
				String weightStr = weightObj == null ? "1" : weightObj.toString();
				DataSourceManager.weights.put(dsPrefix, Integer.parseInt(weightStr));
			}
		}
	}

	
	private DataSource buildDataSource(Map<String, Object> dsMap, Environment env) {
		try {
			Object type = dsMap.get("type");
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;
			}
			if(!type.equals(DATASOURCE_TYPE_DEFAULT)){
				throw new RuntimeException("暂不支持非DruidDataSource数据源!");
			}
			Class<? extends DataSource> dataSourceType;
			dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
			String driverClassName = dsMap.get("driver-class-name").toString();
			String url = dsMap.get("url").toString();
			String username = decrypt(dsMap.get("username").toString());
			String password = decrypt(dsMap.get("password").toString());




			Binder binder = Binder.get(env);
			Properties datasourceProp = binder.bind("spring.datasource", Bindable.of(Properties.class)).get();
			datasourceProp.putAll(dsMap);
			return DruidDataSourceFactory.createDataSource(datasourceProp);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private String decrypt(String value){
		if(StringUtils.isBlank(value)) {
			return value;
		}
		if(value.startsWith("ENC(") && value.endsWith(")")){
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(SsConstants.ENCRYPT_PROPERTY_PASSWORD);
			return textEncryptor.decrypt(value.substring(4, value.length()-1));
		}else {
			return value;
		}
	}

	
	private void dataBinder(DataSource dataSource, Environment env){






































	}




}