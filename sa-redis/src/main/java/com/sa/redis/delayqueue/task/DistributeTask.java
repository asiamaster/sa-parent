package com.sa.redis.delayqueue.task;

import com.sa.redis.delayqueue.consts.DelayQueueConstants;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;




@Component
@ConditionalOnExpression("'${ss.delayqueue.distributed.enable}'=='true'")
public class DistributeTask {

    private static final String LUA_SCRIPT;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    static {






















        StringBuilder sb = new StringBuilder(512);





        sb.append("local val = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1])\n");

        sb.append("if(next(val) ~= nil) then\n");

        sb.append("    redis.call('sadd', KEYS[2], ARGV[2])\n");

        sb.append("    redis.call('zremrangebyscore', KEYS[1], '-inf', ARGV[1])\n");

        sb.append("  for i = 1, #val, 1 do\n");



        sb.append("    redis.call('rpush', KEYS[3], val[i])\n");
        sb.append("  end\n");
        sb.append("  return 1\n");
        sb.append("end\n");
        sb.append("return 0");
        LUA_SCRIPT = sb.toString();
    }


    @Scheduled(cron = "${ss.distributeTask.scheduled:0/5 * * * * ?}")
    public void scheduledTask() {
        try {

            Set<String> waitTopics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC_WAIT);
            assert waitTopics != null;
            for (String waitTopic : waitTopics) {
                if (!redisTemplate.hasKey(waitTopic)) {

                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC_WAIT, waitTopic);
                    continue;
                }
                String activeTopic = waitTopic.replace(DelayQueueConstants.DELAY_WAIT_KEY, DelayQueueConstants.DELAY_ACTIVE_KEY);
                Object[] keys = new Object[]{serialize(waitTopic), serialize(DelayQueueConstants.META_TOPIC_ACTIVE), serialize(activeTopic)};
                Object[] values = new Object[]{serialize(String.valueOf(System.currentTimeMillis())), serialize(activeTopic)};
                Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                    Object nativeConnection = connection.getNativeConnection();
                    if (nativeConnection instanceof RedisAsyncCommands) {
                        RedisAsyncCommands commands = (RedisAsyncCommands) nativeConnection;
                        return (Long) commands.getStatefulConnection().sync().eval(LUA_SCRIPT, ScriptOutputType.INTEGER, keys, values);
                    } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                        RedisAdvancedClusterAsyncCommands commands = (RedisAdvancedClusterAsyncCommands) nativeConnection;
                        return (Long) commands.getStatefulConnection().sync().eval(LUA_SCRIPT, ScriptOutputType.INTEGER, keys, values);
                    }
                    return 0L;
                });
                if(result != null && result > 0) {
                    logger.debug("消息到期进入执行队列({})", activeTopic);
                }
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