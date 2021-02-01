package com.sa.idempotent.service;


import com.sa.idempotent.dto.TokenPair;

public interface IdempotentTokenService {


    TokenPair getToken(String url);


}
