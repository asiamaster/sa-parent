package com.sa.rocketmq;

import java.util.ArrayList;
import java.util.List;

public class ConsummerConfig {
	private String namesrvAddr = null;
	private String consumerGroup = null;
	private boolean autoScan = Boolean.TRUE.booleanValue();
	private ConsummerType consummerType = ConsummerType.ConcurrentConsummer;
	private List<RocketMQListener> mqListeners = new ArrayList();

	public ConsummerConfig(String namesrvAddr, String consumerGroup, List<RocketMQListener> mqListeners) {
		this.namesrvAddr = namesrvAddr;
		this.consumerGroup = consumerGroup;
		this.mqListeners = mqListeners;
	}

	public ConsummerConfig() {
	}

	public boolean isAutoScan() {
		return this.autoScan;
	}

	public void setAutoScan(boolean autoScan) {
		this.autoScan = autoScan;
	}

	public ConsummerType getConsummerType() {
		return this.consummerType;
	}

	public void setConsummerType(ConsummerType consummerType) {
		this.consummerType = consummerType;
	}

	public String getNamesrvAddr() {
		return this.namesrvAddr;
	}

	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}

	public String getConsumerGroup() {
		return this.consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public List<RocketMQListener> getMqListeners() {
		return this.mqListeners;
	}

	public void setMqListeners(List<RocketMQListener> mqListeners) {
		this.mqListeners = mqListeners;
	}
}
