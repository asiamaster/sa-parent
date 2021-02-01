
package com.sa.exception;

import com.sa.constant.ResultCode;


public class AppException extends InternalException {
	private static final long serialVersionUID = 12489037178905L;
    private static final String DEFAULT_MESSAGE = "应用程序异常!";
	private String errorData;

	public AppException() {
		super(DEFAULT_MESSAGE);
	}
	
	public AppException(String message) {
		super(message);
		this.code = ResultCode.SERVICE_UNAVALIABLE;
	}
	
	public AppException(String message, Throwable cause) {
		super(message, cause);
        this.code = ResultCode.SERVICE_UNAVALIABLE;
	}
	
	public AppException(Throwable cause) {
		super(cause);
        this.code = ResultCode.SERVICE_UNAVALIABLE;
	}

	public AppException(String code, String message) {
	    super(message);
	    this.code=code;
    }
	
	public AppException(String code, String errorData, String message) {
        super(message);
        this.code=code;
        this.message = message;
        this.errorData=errorData;
    }

    @Override
    public String getCode() {
        return code;
    }

    public String getErrorData() {
        return errorData;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [code=" + getCode() + ", errorData="
                + getErrorData() + ", message=" + getMessage()
                + ", cause=" + getCause() + "]";
    }
}
