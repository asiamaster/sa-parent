package com.sa.rocketmq.impl;

import com.sa.rocketmq.ProducerConfig;
import com.sa.rocketmq.RocketMQProducer;
import com.sa.rocketmq.exception.RocketMqException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class RocketMQProducerFactory extends AbstractFactoryBean<RocketMQProducer>
{
  private ProducerConfig producerConfig;

  public ProducerConfig getProducerConfig()
  {
    return this.producerConfig;
  }

  public void setProducerConfig(ProducerConfig producerConfig) {
    this.producerConfig = producerConfig;
  }

  @Override
  public Class<RocketMQProducer> getObjectType()
  {
    return RocketMQProducer.class;
  }

  @Override
  protected RocketMQProducer createInstance() throws Exception
  {
    checkConfig();
    RocketMQProducerImpl producer = new RocketMQProducerImpl();
    producer.setNamesrvAddr(this.producerConfig.getNamesrvAddr());
    producer.setProducerGroup(this.producerConfig.getProducerGroup());
    return producer;
  }
  private boolean checkConfig() {
    if (this.producerConfig == null) {
      throw new IllegalArgumentException("config 不能为空");
    }
    if (StringUtils.isBlank(this.producerConfig.getNamesrvAddr())) {
      throw new IllegalArgumentException("namesrvAddr 不能为空");
    }
    if (StringUtils.isBlank(this.producerConfig.getProducerGroup())) {
      throw new IllegalArgumentException("producerGroup 不能为空");
    }

    return true;
  }

  public void start()
  {
  }

  public void shutdown() throws RocketMqException, Exception
  {
    ((RocketMQProducer)getObject()).shutdown();
  }
}