package com.sa.datasource.selector;

import com.sa.datasource.DataSourceManager;
import com.sa.datasource.strategy.WeightedRoundRobinStrategy;

import java.util.List;
import java.util.Map;


public class WeightedRoundRobinSelector extends OneMasterMultiSlavesDataSourceSelector {

	private WeightedRoundRobinStrategy strategy;

	public WeightedRoundRobinSelector() {
		this.strategy = new WeightedRoundRobinStrategy(filteredWeights(DataSourceManager.slaves, DataSourceManager.weights));
	}

	private int[] filteredWeights(List<String> slaves, Map<String, Integer> weights) {
		int[] weightArr = new int[slaves.size()];
		for (int i = 0; i < slaves.size(); i++) {
			String slave = slaves.get(i);
			weightArr[i] = weights.get(slave);
		}
		return weightArr;
	}

	@Override
	protected String fetchSlave() {
		return DataSourceManager.fetchSlave(strategy.next());
	}

}
