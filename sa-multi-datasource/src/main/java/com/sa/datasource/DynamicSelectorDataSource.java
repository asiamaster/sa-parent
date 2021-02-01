package com.sa.datasource;

import com.sa.datasource.aop.MasterSlaveContextHolder;
import com.sa.datasource.selector.OneMasterMultiSlavesDataSourceSelector;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class DynamicSelectorDataSource extends AbstractRoutingDataSource {

	OneMasterMultiSlavesDataSourceSelector dataSourceSelector;

	public void setDataSourceSelector(OneMasterMultiSlavesDataSourceSelector dataSourceSelector) {
		this.dataSourceSelector = dataSourceSelector;
	}

	public OneMasterMultiSlavesDataSourceSelector getDataSourceSelector() {
		return dataSourceSelector;
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return dataSourceSelector.fetch(MasterSlaveContextHolder.writable());
	}

}
