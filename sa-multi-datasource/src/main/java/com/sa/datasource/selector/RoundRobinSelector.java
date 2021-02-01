package com.sa.datasource.selector;

import com.sa.datasource.DataSourceManager;
import com.sa.datasource.strategy.RoundRobinStrategy;


public class RoundRobinSelector extends OneMasterMultiSlavesDataSourceSelector {

	private RoundRobinStrategy strategy;

	public RoundRobinSelector() {
		strategy = new RoundRobinStrategy(DataSourceManager.slaves.size());
	}

	@Override
	protected String fetchSlave() {
		return DataSourceManager.slaves.get(strategy.next());
	}

}
