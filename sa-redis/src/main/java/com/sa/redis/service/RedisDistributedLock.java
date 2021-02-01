package com.sa.redis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


@Component
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class RedisDistributedLock{

    private final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    protected RedisTemplate redisTemplate;

    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }


    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }


    @Resource
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }


    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    public boolean tryGetLock(String key, String value,  long expire) {
        try {
            RedisCallback<Boolean> callback = (connection) -> {
                return connection.set(key.getBytes(Charset.forName("UTF-8")), value.getBytes(Charset.forName("UTF-8")), Expiration.seconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
            };
            return (Boolean)redisTemplate.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean tryGetLockSync(String key, String value, long expire) {
        return tryGetLockSync(key, value, expire, Long.MAX_VALUE, TimeUnit.SECONDS, Long.MAX_VALUE, 10L, TimeUnit.MILLISECONDS);
    }




    public boolean tryGetLockSync(String key, String value, long expire, long awaitTime, TimeUnit awaitUnit, long retryCount, long sleepTime, TimeUnit sleepUnit) {
        if(tryGetLock(key, value, expire)){
            return true;
        }

        long nanos = awaitUnit.toNanos(awaitTime);
        final long deadline = System.nanoTime() + nanos;
        int count = 0;
        while (true) {
            nanos = deadline - System.nanoTime();

            if (nanos <= 0L) {
                return false;
            }
            if (tryGetLock(key, value, expire)) {
                return true;
            }

            if (count++ > retryCount || Thread.interrupted()) {
                return false;
            }

            LockSupport.parkNanos(sleepUnit.toNanos(sleepTime));
        }
    }



    public boolean releaseLock(String lockKey, String lockValue) {
        RedisCallback<Boolean> callback = (connection) -> {
            return connection.eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN ,1, lockKey.getBytes(Charset.forName("UTF-8")), lockValue.getBytes(Charset.forName("UTF-8")));
        };
        return (Boolean)redisTemplate.execute(callback);
    }
}
