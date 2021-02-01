package com.sa.idempotent.controller;

import com.sa.idempotent.dto.TokenPair;
import com.sa.idempotent.service.IdempotentTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/idempotentToken")
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentTokenController {

    @Autowired
    private IdempotentTokenService idempotentTokenService;


    @ResponseBody
    @GetMapping("/getToken.api")
    public TokenPair getToken(@RequestParam("url") String url) {
        return idempotentTokenService.getToken(url);
    }


}