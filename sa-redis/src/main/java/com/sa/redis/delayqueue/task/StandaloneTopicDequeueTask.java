package com.sa.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.sa.component.CustomThreadPoolExecutorCache;
import com.sa.redis.delayqueue.annotation.StreamListener;
import com.sa.redis.delayqueue.component.BeanMethodCacheComponent;
import com.sa.redis.delayqueue.consts.DelayQueueConstants;
import com.sa.redis.delayqueue.dto.DelayMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



@Component
@ConditionalOnExpression("'${ss.delayqueue.standalone.enable}'=='true'")
public class StandaloneTopicDequeueTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private BeanMethodCacheComponent beanMethodCacheComponent;
    @Resource
    private CustomThreadPoolExecutorCache customThreadPoolExecutorCache;


    @Scheduled(cron = "${ss.standaloneTopicDequeueTask.scheduled:0/1 * * * * ?}")
    public void scheduledTask() {
        try {

            Set<String> topics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC);
            Map<Object, Method> beanMethod = beanMethodCacheComponent.getBeanMethod(StreamListener.class);
            for (String topic : topics) {
                if (!redisTemplate.hasKey(topic)) {

                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC, topic);
                    continue;
                }
                Long startTime = System.currentTimeMillis();

                Set<String> sets = redisTemplate.opsForZSet().rangeByScore(topic, 0, startTime);
                if(sets.isEmpty()){
                    continue;
                }
                try {
                    Iterator<String> iterator = sets.iterator();
                    String delayMessageJson = null;
                    while (iterator.hasNext()) {
                        delayMessageJson = iterator.next();
                        for (Map.Entry<Object, Method> entry : beanMethod.entrySet()) {
                            DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                            StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                            if (!streamListener.value().equals(message.getTopic())) {
                                continue;
                            }
                            String finalDelayMessageJson = delayMessageJson;
                            customThreadPoolExecutorCache.getExecutor(DelayQueueConstants.DELAY_QUEUE_EXECUTOR_KEY).submit(() -> {
                                try {
                                    logger.debug("消息到期发送到消息监听器, topic: {}", message.getTopic());
                                    entry.getValue().invoke(entry.getKey(), message);
                                } catch (Throwable t) {

                                    String failKey = topic.replace(DelayQueueConstants.DELAY_QUEUE_KEY, DelayQueueConstants.DELAY_QUEUE_FAIL_KEY);
                                    redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                    logger.warn("延时队列任务处理异常: ", t);
                                }
                            });

                            break;
                        }
                    }
                }finally {
                    redisTemplate.opsForZSet().removeRangeByScore(topic, 0, startTime);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
