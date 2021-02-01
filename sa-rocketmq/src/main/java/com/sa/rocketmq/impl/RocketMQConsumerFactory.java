package com.sa.rocketmq.impl;

import com.sa.rocketmq.ConsummerConfig;
import com.sa.rocketmq.ConsummerType;
import com.sa.rocketmq.RocketMQConsumer;
import com.sa.rocketmq.RocketMQListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RocketMQConsumerFactory extends AbstractFactoryBean<RocketMQConsumer>
		implements ApplicationContextAware {
	private ApplicationContext context;
	private ConsummerConfig consummerConfig;

	private boolean checkConfig() {
		if (this.consummerConfig == null) {
			throw new IllegalArgumentException("config 不能为空");
		}
		if (StringUtils.isBlank(this.consummerConfig.getNamesrvAddr())) {
			throw new IllegalArgumentException("namesrvAddr 不能为空");
		}
		if (StringUtils.isBlank(this.consummerConfig.getConsumerGroup())) {
			throw new IllegalArgumentException("consumerGroup 不能为空");
		}
		List mqlist = this.consummerConfig.getMqListeners();
		if (mqlist == null) {
			mqlist = new ArrayList();
			this.consummerConfig.setMqListeners(mqlist);
		}
		if (mqlist.isEmpty()) {
			if (this.context == null)
				throw new IllegalArgumentException("mqListeners 不能为空");
			if (this.consummerConfig.isAutoScan()) {
				Map beansOfType = this.context.getBeansOfType(RocketMQListener.class);
				mqlist.addAll(beansOfType.values());
			}
			if (mqlist.isEmpty()) {
				throw new IllegalArgumentException("mqListeners 不能为空");
			}
		}
		return true;
	}

	private RocketMQConsumer getInstance() {
		checkConfig();

		RocketMQConsumerImpl consumerImpl = null;
		if (ConsummerType.ConcurrentConsummer == this.consummerConfig.getConsummerType())
			consumerImpl = new RocketMQConsumerImpl(this.consummerConfig.getMqListeners());
		else if (ConsummerType.OrderConsummer == this.consummerConfig.getConsummerType())
			consumerImpl = new RocketMQOrderlyConsumerImpl(this.consummerConfig.getMqListeners());
		else {
			throw new RuntimeException("ConsummerType不正确,未能创建相应的Consummer");
		}
		consumerImpl.setNamesrvAddr(this.consummerConfig.getNamesrvAddr());
		consumerImpl.setProducerGroup(this.consummerConfig.getConsumerGroup());
		return consumerImpl;
	}

	public void startListener() throws Exception {
		((RocketMQConsumer) getObject()).startListener();
	}

	public void stopListener() throws Exception {
		((RocketMQConsumer) getObject()).stopListener();
	}

	public ConsummerConfig getConsummerConfig() {
		return this.consummerConfig;
	}

	public void setConsummerConfig(ConsummerConfig consummerConfig) {
		this.consummerConfig = consummerConfig;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public Class<RocketMQConsumer> getObjectType() {
		return RocketMQConsumer.class;
	}

	@Override
	protected RocketMQConsumer createInstance() throws Exception {
		return getInstance();
	}
}
