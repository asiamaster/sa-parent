package com.sa.datasource;

import com.sa.datasource.aop.DynamicRoutingDataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		return DynamicRoutingDataSourceContextHolder.peek();
	}

}
