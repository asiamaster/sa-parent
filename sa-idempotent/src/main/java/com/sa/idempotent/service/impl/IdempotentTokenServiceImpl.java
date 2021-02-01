package com.sa.idempotent.service.impl;

import com.sa.idempotent.dto.TokenPair;
import com.sa.idempotent.service.IdempotentTokenService;
import com.sa.redis.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentTokenServiceImpl implements IdempotentTokenService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public TokenPair getToken(String url) {
        TokenPair tokenPair = new TokenPair();
        String tokenValue = UUID.randomUUID().toString();
        tokenPair.setKey(url + tokenValue);
        tokenPair.setValue(tokenValue);
        redisUtil.set(url + tokenValue, tokenValue);
        return tokenPair;
    }

}
