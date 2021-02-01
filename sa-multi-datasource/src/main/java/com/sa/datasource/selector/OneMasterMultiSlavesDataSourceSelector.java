package com.sa.datasource.selector;


import com.sa.datasource.DataSourceManager;


public abstract class OneMasterMultiSlavesDataSourceSelector extends DataSourceSelector {


	@Override
	public String fetch(boolean writable) {
		return writable ? DataSourceManager.master : fetchSlave();
	}

	@Override
	public String fetchDefault() {
		return DataSourceManager.getDefault();
	}


	protected abstract String fetchSlave();

}
