package com.sa.redis.service;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("unchecked")
@Component
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class RedisUtil {
    @SuppressWarnings("rawtypes")
    @Resource(name="redisTemplate")
    protected RedisTemplate redisTemplate;

    
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }
    
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }
    
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    
    public Boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }
    
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    
    public Long lPush(String k, Object v) {
        return redisTemplate.opsForList().rightPush(k, v);
    }

    
    public List<Object> lRange(String k, long start, long end) {
        return redisTemplate.opsForList().range(k, start, end);
    }

    
    public <T> T get(final String key, Class<T> clazz) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        if(null != result && result.getClass().isAssignableFrom(clazz)){
            return (T)result;
        }
        return result == null ? null : JSON.parseObject(result.toString(), clazz);
    }

    
    public Long increment(String key, Long value){
        return redisTemplate.opsForValue().increment(key, value);
    }

    
    public void set(final String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    
    public Long add(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    
    public Boolean zAdd(String key, Object value, double scoure) {
        return redisTemplate.opsForZSet().add(key, value, scoure);
    }

    
    public Set<Object> rangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    
    public void set(final String key, Object value, Long expireTime) {
        set(key, value, expireTime, TimeUnit.SECONDS);
    }

    
    public void set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    
    public Boolean setIfAbsent(final String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    
    public boolean setIfAbsent(final String key, Object value, Long expireTime) {
        return setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
    }

    
    public Boolean setIfAbsent(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, timeUnit);
    }

    
    public Boolean expire(String key, long timeout, TimeUnit timeUnit){
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    
    public Boolean expireAt(String key, Date date){
        return redisTemplate.expireAt(key, date);
    }
}
