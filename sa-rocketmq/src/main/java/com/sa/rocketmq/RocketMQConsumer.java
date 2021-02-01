package com.sa.rocketmq;

import com.sa.rocketmq.exception.RocketMqException;

public interface RocketMQConsumer {
	void startListener()
			throws RocketMqException;

	void stopListener()
			throws RocketMqException;
}
