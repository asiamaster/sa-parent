package com.sa.rocketmq.impl;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.sa.rocketmq.RocketMQConsumer;
import com.sa.rocketmq.RocketMQListener;
import com.sa.rocketmq.exception.RocketMqException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class RocketMQConsumerImpl
		implements RocketMQConsumer {
	private static Log log = LogFactory.getLog(RocketMQConsumerImpl.class);
	private static DefaultMQPushConsumer consumer;
	private static RocketMQConsumerImpl SIN;
	private Boolean started = Boolean.valueOf(false);
	private Map<String, Map<String, List<RocketMQListener>>> listenerMap;

	@Value("${mq.namesrvAddr}")
	private String namesrvAddr = "";

	@Value("${mq.producerGroup}")
	private String producerGroup = "";

	@Autowired(required = false)
	private List<RocketMQListener> mqListeners;

	public RocketMQConsumerImpl() {
		this.listenerMap = new HashMap();
		SIN = this;
	}

	public RocketMQConsumerImpl(List<RocketMQListener> mqListeners) {
		this.mqListeners = mqListeners;
		this.listenerMap = new HashMap();
		SIN = this;
	}

	public static RocketMQConsumerImpl me() {
		return SIN;
	}

	public static DefaultMQPushConsumer consumer() throws RocketMqException {
		if (consumer == null) {
			if (StringUtils.isBlank(me().producerGroup))
				consumer = new DefaultMQPushConsumer();
			else {
				consumer = new DefaultMQPushConsumer(me().producerGroup);
			}
			if (StringUtils.isBlank(me().namesrvAddr)) {
				throw new RocketMqException("MQ服务器地址为空!");
			}


			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			consumer.setNamesrvAddr(me().namesrvAddr);
			consumer.setVipChannelEnabled(false);
			consumer.setInstanceName("consumer_instance");
		}
		return consumer;
	}

	public static DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public static void setConsumer(DefaultMQPushConsumer consumer) {
		consumer = consumer;
	}

	@Override
	public void startListener()
			throws RocketMqException {
		if (this.started.booleanValue()) {
			return;
		}
		this.started = Boolean.valueOf(true);

		if ((this.mqListeners == null) || (this.mqListeners.size() <= 0)) {
			if (log.isInfoEnabled()) {
				log.info("没有监听器, 不启动MQ消息监听!");
			}
			return;
		}
		try {
			if (log.isInfoEnabled()) {
				log.info("开始启动MQ客户端!");
			}
			initListenerMap();

			subscribe();

			registerListener();
			consumer().start();
			if (log.isInfoEnabled())
				log.info("MQ Client 启动成功!");
		} catch (MQClientException e) {
			log.error("MQ Client启动失败!" + e.getMessage(), e);
		}
	}

	@Override
	public void stopListener() throws RocketMqException {
		if (log.isInfoEnabled()) {
			log.info("开始停止MQ客户端!");
		}
		consumer().shutdown();
		if (log.isInfoEnabled()) {
			log.info("MQ Client 停止成功!");
		}
		consumer = null;
	}

	private void subscribe()
			throws RocketMqException, MQClientException {
		for (RocketMQListener listener : this.mqListeners) {
			String topic = listener.getTopic();
			String tags = listener.getTags();
			if (log.isInfoEnabled()) {
				log.info("订阅: topic->" + topic + "   tags->" + tags);
			}
			consumer().subscribe(topic, tags);
		}
	}

	private void initListenerMap() {
		RocketMQListener listener;
		Map topic;
		for (Iterator i$ = this.mqListeners.iterator(); i$.hasNext(); ) {
			listener = (RocketMQListener) i$.next();
			topic = (Map) this.listenerMap.get(listener.getTopic());
			if (topic == null) {
				topic = new HashMap();
				this.listenerMap.put(listener.getTopic(), topic);
			}
			List<String> selfTag = splitMsg(listener.getTags(), "\\|\\|");
			for (String tag : selfTag) {
				List items = (List) topic.get(tag);
				if (items == null) {
					items = new ArrayList();
					topic.put(tag, items);
				}
				if (!items.contains(listener)) {
					items.add(listener);
				}
			}
		}
	}

	protected List<RocketMQListener> fetchListener(String topic, String tags) {
		List list = new ArrayList();
		if (!this.listenerMap.containsKey(topic)) {
			return list;
		}
		Map<String, Map<String, List<RocketMQListener>>> tagMap = (Map) this.listenerMap.get(topic);
		List<String> selfTag = splitMsg(tags, "\\|\\|");
		for (String tag : selfTag) {
			List<RocketMQListener> listeners = (List) tagMap.get(tag);
			for (RocketMQListener mql : listeners) {
				if (!list.contains(mql)) {
					list.add(mql);
				}
			}
		}
		return list;
	}

	private void registerListener()
			throws RocketMqException {
		consumer().registerMessageListener(createMesageListener());
	}


	protected MessageListener createMesageListener() {
		return new MessageListenerConcurrently() {
			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
				Boolean success = false;
				MessageExt msg;
				for (Iterator i$ = msgs.iterator(); i$.hasNext(); ) {
					msg = (MessageExt) i$.next();
					List<RocketMQListener> listeners = RocketMQConsumerImpl.this.fetchListener(msg.getTopic(), msg.getTags());
					for (RocketMQListener mql : listeners)
						try {
							mql.operate(msg);
							success = true;
						} catch (Exception e) {
							RocketMQConsumerImpl.log.warn("处理MQ消息失败!", e);
						}
				}

				if (success.booleanValue()) {
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
				return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			}
		};
	}

	private Boolean equalsTag(List<String> msgTags, String tags) {
		if (StringUtils.isBlank(tags)) {
			return Boolean.valueOf(true);
		}
		if ("*".equals(tags)) {
			return Boolean.valueOf(true);
		}
		List<String> selfTag = splitMsg(tags, "\\|\\|");
		for (String key : selfTag) {
			if (msgTags.contains(key)) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}

	private List<String> splitMsg(String tags, String regex) {
		String[] arr = tags.split(regex);
		List list = new ArrayList();
		for (String t : arr) {
			list.add(t.trim());
		}
		return list;
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
