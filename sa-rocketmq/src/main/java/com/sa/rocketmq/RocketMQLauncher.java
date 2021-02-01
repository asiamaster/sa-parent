package com.sa.rocketmq;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.sa.rocketmq.exception.RocketMqException;
import com.sa.rocketmq.impl.RocketMQConsumerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${rocketmq.enable}'=='true'")
public class RocketMQLauncher
		implements ApplicationListener {
	private Log log = LogFactory.getLog(RocketMQLauncher.class);
	private RocketMQConsumer rocketMQConsumer;
	private DefaultMQPushConsumer consumer;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if ((event instanceof ContextRefreshedEvent)) {
			start((ContextRefreshedEvent) event);
			return;
		}
		else if ((event instanceof ContextStoppedEvent)) {
			stop(event);
		}
		else if ((event instanceof ContextClosedEvent)) {
			stop(event);
		}
	}

	public void start(ContextRefreshedEvent event) {
		try {
			if (this.rocketMQConsumer == null) {
				RocketMQConsumerImpl dmq = new RocketMQConsumerImpl();
				if (this.consumer != null) {
					RocketMQConsumerImpl.setConsumer(this.consumer);
				}
				this.rocketMQConsumer = dmq;

				event.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this.rocketMQConsumer);
				this.rocketMQConsumer.startListener();
			}
		} catch (RocketMqException e) {
			this.log.error(e.getMessage(), e);
		}
	}

	public void stop(ApplicationEvent event) {
		try {
			if (this.rocketMQConsumer != null)
				this.rocketMQConsumer.stopListener();
		} catch (RocketMqException e) {
			this.log.error(e.getMessage(), e);
		}
	}

	public DefaultMQPushConsumer getConsumer() {
		return this.consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}
}
