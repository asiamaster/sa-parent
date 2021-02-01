package com.sa.rocketmq.impl;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.sa.rocketmq.RocketMQProducer;
import com.sa.rocketmq.exception.RocketMqException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnExpression("'${rocketmq.enable}'=='true'")
public class RocketMQProducerImpl
		implements RocketMQProducer {
	private static Log log = LogFactory.getLog(RocketMQProducerImpl.class);
	private static DefaultMQProducer producer;
	private static RocketMQProducerImpl SIN;
	private static Lock lock = new ReentrantLock();

	@Value("${mq.namesrvAddr}")
	private String namesrvAddr = "";

	@Value("${mq.producerGroup}")
	private String producerGroup = "";

	public RocketMQProducerImpl()
			throws RocketMqException {
		SIN = this;
	}

	private static RocketMQProducerImpl me() {
		return SIN;
	}

	public static DefaultMQProducer producer() throws RocketMqException {
		try {
			lock.lock();

			if (producer == null) {
				if (StringUtils.isBlank(me().producerGroup))
					producer = new DefaultMQProducer();
				else {
					producer = new DefaultMQProducer(me().producerGroup);
				}
				if (StringUtils.isBlank(me().namesrvAddr)) {
					throw new RocketMqException("MQ服务器地址为空!");
				}
				producer.setNamesrvAddr(me().namesrvAddr);
				producer.setVipChannelEnabled(false);
				producer.start();
			}
		} catch (MQClientException e) {
			try {
				producer.shutdown();
			} catch (Exception e1) {
				throw new RocketMqException("关闭MQ出错!");
			}
			producer = null;
			log.error("启动MQ出错:" + e.getMessage(), e);
			throw new RocketMqException("启动MQ出错!");
		} finally {
			lock.unlock();
		}
		return producer;
	}
	@Override
	public void sendMsg(Message msg) throws RocketMqException {
		try {
			DefaultMQProducer defaultMQProducer = producer();
			defaultMQProducer.send(msg);
		} catch (RocketMqException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RocketMqException("操作MQ出错!");
		}
	}
	@Override
	public void sendOrderMsg(Message msg, Number order) throws RocketMqException {
		try {
			producer().send(msg, new MessageQueueSelector() {
						@Override
						public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
							Number id = (Number) arg;
							int index = (int) (id.longValue() % mqs.size());
							return (MessageQueue) mqs.get(index);
						}
					}
					, order);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RocketMqException("操作MQ出错！");
		}
	}
	@Override
	public SendResult sendOrderMsg(Message msg, String order) throws RocketMqException {
		try {
			return producer().send(msg, new SelectMessageQueueByHash(), order);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		throw new RocketMqException("操作MQ出错！");
	}
	@Override
	public void shutdown() throws RocketMqException {
		producer().shutdown();
	}

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
