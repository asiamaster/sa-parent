package com.sa.datasource;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SwitchDataSource {
	public static final String DEFAULT_DATASOURCE = "datasource";


	@AliasFor("name")
	String value() default SwitchDataSource.DEFAULT_DATASOURCE;


	@AliasFor("value")
	String name() default SwitchDataSource.DEFAULT_DATASOURCE;


	DataSourceType type() default DataSourceType.MASTER;
}
