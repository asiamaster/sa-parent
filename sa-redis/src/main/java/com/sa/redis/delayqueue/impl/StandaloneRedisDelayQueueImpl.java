package com.sa.redis.delayqueue.impl;

import com.alibaba.fastjson.JSON;
import com.sa.redis.delayqueue.RedisDelayQueue;
import com.sa.redis.delayqueue.consts.DelayQueueConstants;
import com.sa.redis.delayqueue.dto.DelayMessage;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@ConditionalOnExpression("'${ss.delayqueue.standalone.enable}'=='true'")
public class StandaloneRedisDelayQueueImpl<E extends DelayMessage> implements RedisDelayQueue<E> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;


    @Override
    public void poll() {

    }


    @Override
    public void push(E e) {
        try {
            String jsonStr = JSON.toJSONString(e);
            String topic = e.getTopic();
            String zkey =  DelayQueueConstants.DELAY_QUEUE_KEY + topic;

            String script = "redis.call('sadd', KEYS[1], ARGV[1])\n" +
                            "redis.call('zadd', KEYS[2], ARGV[2], ARGV[3])\n" +
                            "return 1";
            Object[] keys = new Object[]{serialize(DelayQueueConstants.META_TOPIC), serialize(zkey)};

            Long score = e.getDelayTime() != null ? e.getDelayTime() : System.currentTimeMillis() + (e.getDelayDuration() * 1000);
            Object[] values = new Object[]{ serialize(zkey), serialize(String.valueOf(score)), serialize(jsonStr)};
            Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof RedisAsyncCommands) {
                    RedisAsyncCommands commands = (RedisAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                    RedisAdvancedClusterAsyncCommands commands = (RedisAdvancedClusterAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                }
                return 0L;
            });
            if(result != null && result > 0) {
                logger.debug("消息推送成功进入延时队列, topic: {}", e.getTopic());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        return stringRedisSerializer.serialize(key);
    }

}
