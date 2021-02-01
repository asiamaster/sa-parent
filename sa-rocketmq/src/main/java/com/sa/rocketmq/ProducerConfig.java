package com.sa.rocketmq;

public class ProducerConfig {
	private String namesrvAddr = null;
	private String producerGroup = null;

	public String getNamesrvAddr() {
		return this.namesrvAddr;
	}

	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}

	public String getProducerGroup() {
		return this.producerGroup;
	}

	public void setProducerGroup(String producerGroup) {
		this.producerGroup = producerGroup;
	}
}
