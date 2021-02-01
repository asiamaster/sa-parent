package com.sa.redis.delayqueue.dto;

import com.sa.dto.IDTO;

import java.time.LocalDateTime;


public interface DelayMessage extends IDTO {


  String getId();
  void setId(String id);



  String getTopic();
  void setTopic(String topic);


  String getBody();
  void setBody(String body);


  Long getDelayTime();
  void setDelayTime(Long delayTime);


  Long getDelayDuration();
  void setDelayDuration(Long delayDuration);


  LocalDateTime getCreateTime();
  void setCreateTime(LocalDateTime createTime);
}
