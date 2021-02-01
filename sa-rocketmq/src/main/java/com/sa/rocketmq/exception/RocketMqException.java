package com.sa.rocketmq.exception;

import java.util.Map;


public class RocketMqException extends Exception {
	private Map<?, ?> map = null;

	public RocketMqException() {
	}

	public RocketMqException(String msg) {
		super(msg);
	}

	public RocketMqException(String msg, Map<?, ?> map) {
		super(msg);
		this.setMap(map);
	}

	public Map<?, ?> getMap() {
		return this.map;
	}

	public void setMap(Map<?, ?> map) {
		this.map = map;
	}
}
