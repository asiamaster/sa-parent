package com.sa.datasource.strategy;

import org.apache.commons.lang3.ArrayUtils;


public class WeightedRoundRobinStrategy extends BalanceStrategy {


	private int[] weights;


	private int[] failed;


	private int maxWeight;


	private static int index = -1;


	private int maxStep;


	private int currentWeight;

	public WeightedRoundRobinStrategy(int[] weights) {
		this.weights = weights;
		maxWeight = maxWeight(weights);
		if (maxWeight <= 0)
			throw new IllegalArgumentException("the max weight should greate than 0");
		maxStep = calculateMaxStep(weights);
	}


	private int calculateMaxStep(int[] weights) {
		int min = minWeight(weights);
		boolean isCommon = true;
		while (min >= 1) {
			for (int weight : weights) {
				if (weight % min != 0){
					isCommon = false;
					break;
				}
			}
			if (isCommon)
				break;
			min--;
		}
		return min;
	}

	private int minWeight(int[] weights) {
		int min = Integer.MAX_VALUE;
		for (int each : weights)
			if (each < min && each != 0)
				min = each;
		return min;
	}

	private int maxWeight(int[] weights) {
		int max = Integer.MIN_VALUE;
		for (int each : weights)
			if (each > max)
				max = each;
		return max;
	}

	@Override
	public int next() {
		reWeighting(failed);
		while (true) {
			index = (index + 1) % weights.length;
			if (index == 0) {
				currentWeight = currentWeight - maxStep;
				if (currentWeight <= 0)
					currentWeight = maxWeight;
			}
			if (weights[index] >= currentWeight)
				return index;
		}
	}

	private void reWeighting(int[] failed) {
		if (ArrayUtils.isEmpty(failed)) {
			for (int i = 0; i < weights.length; i++)
				if (ArrayUtils.contains(failed, weights[i]))
					weights[i] = 0;
		}
	}

	public static void main(String... args) {
		int[] weights = new int[] { 1, 2, 0 };
		WeightedRoundRobinStrategy strategy = new WeightedRoundRobinStrategy(weights);
		for (int i = 0; i < 100; i++) {
			System.out.println(strategy.next());
		}
	}

	public WeightedRoundRobinStrategy adjust(int[] failed) {
		this.failed = failed;
		return this;
	}

}
