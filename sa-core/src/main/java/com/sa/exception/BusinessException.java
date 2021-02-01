package com.sa.exception;


public class BusinessException extends InternalException {

    private static final String DEFAULT_MESSAGE = "业务异常!";

    public BusinessException() {
        super(DEFAULT_MESSAGE);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }

}

