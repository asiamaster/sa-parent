package com.sa.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.sa.component.CustomThreadPoolExecutorCache;
import com.sa.redis.delayqueue.annotation.StreamListener;
import com.sa.redis.delayqueue.component.BeanMethodCacheComponent;
import com.sa.redis.delayqueue.consts.DelayQueueConstants;
import com.sa.redis.delayqueue.dto.DelayMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



@Component
@ConditionalOnExpression("'${ss.delayqueue.distributed.enable}'=='true'")
public class HandleTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationContext applicationContext;

    private Map<Object, Method> map = new HashMap<>();

    @Resource
    BeanMethodCacheComponent beanMethodCacheComponent;
    @Resource
    private CustomThreadPoolExecutorCache customThreadPoolExecutorCache;


    @Scheduled(cron = "${ss.handleTask.scheduled:0/3 * * * * ?}")
    public void scheduledTask() {
        try {
            Set<String> activeTopics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC_ACTIVE);
            Map<Object, Method> beanMethod = beanMethodCacheComponent.getBeanMethod(StreamListener.class);
            for (String activeTopic : activeTopics) {
                if (!redisTemplate.hasKey(activeTopic)) {

                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC_ACTIVE, activeTopic);
                    continue;
                }

                String delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                while (StringUtils.isNotBlank(delayMessageJson)) {
                    for (Map.Entry<Object, Method> entry : beanMethod.entrySet()) {
                        DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                        StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                        if(!streamListener.value().equals(message.getTopic())){
                            continue;
                        }
                        String finalDelayMessageJson = delayMessageJson;
                        customThreadPoolExecutorCache.getExecutor(DelayQueueConstants.DELAY_QUEUE_EXECUTOR_KEY).submit(() -> {
                            try {
                                logger.debug("消息到期执行({})", message.getTopic());
                                entry.getValue().invoke(entry.getKey(), message);
                            } catch (Throwable t) {

                                String failKey = activeTopic.replace(DelayQueueConstants.DELAY_ACTIVE_KEY, DelayQueueConstants.DELAY_FAIL_KEY);
                                redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                logger.warn("消息监听器发送异常: ", t);
                            }
                        });
                        break;
                    }
                    delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
