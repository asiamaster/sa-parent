package com.sa.datasource.strategy;

import java.util.concurrent.atomic.AtomicLong;


public class RoundRobinStrategy extends BalanceStrategy {


	private int length;


	private static AtomicLong index = new AtomicLong(0);

	public RoundRobinStrategy(int length) {
		this.length = length;
	}

	@Override
	public int next() {
		return (int) index.getAndIncrement() % length;
	}

}
