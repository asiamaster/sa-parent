package com.sa.redis.delayqueue;

import com.sa.redis.delayqueue.dto.DelayMessage;


public interface RedisDelayQueue<E extends DelayMessage> {


    void poll();


    void push(E e);
}
