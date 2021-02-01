package com.sa.rocketmq.impl;


import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.sa.rocketmq.RocketMQListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;


public class RocketMQOrderlyConsumerImpl extends RocketMQConsumerImpl {
	private static Log log = LogFactory.getLog(RocketMQOrderlyConsumerImpl.class);

	public RocketMQOrderlyConsumerImpl(List<RocketMQListener> mqListeners) {
		super(mqListeners);
	}

	@Override
    protected MessageListener createMesageListener() {
		return new MessageListenerOrderly() {
			@Override
			public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext paramConsumeOrderlyContext) {
				Boolean success = false;
				MessageExt msg;
				for (Iterator i$ = msgs.iterator(); i$.hasNext(); ) {
					msg = (MessageExt) i$.next();
					List<RocketMQListener> listeners = RocketMQOrderlyConsumerImpl.this.fetchListener(msg.getTopic(), msg.getTags());
					for (RocketMQListener mql : listeners)
						try {
							mql.operate(msg);
							success = true;
						} catch (Exception e) {
							RocketMQOrderlyConsumerImpl.log.warn("处理MQ消息失败!", e);
						}
				}

				if (success.booleanValue()) {
					return ConsumeOrderlyStatus.SUCCESS;
				}
				return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
			}
		};
	}
}
