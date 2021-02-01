
package com.sa.exception;

import com.sa.constant.ResultCode;


public class ParamErrorException extends AppException{
	private static final long serialVersionUID = 1790823459807213450L;

	public ParamErrorException() {
		super();
		this.message = "参数错误!";
		this.code = ResultCode.PARAMS_ERROR;
	}
	
	public ParamErrorException(String message) {
		super(message);
		this.code = ResultCode.PARAMS_ERROR;
	}
	
	public ParamErrorException(String message, Throwable cause) {
		super(message, cause);
		this.code = ResultCode.PARAMS_ERROR;
	}
	
	public ParamErrorException(Throwable cause) {
		super(cause);
		this.code = ResultCode.PARAMS_ERROR;
	}
	
    public ParamErrorException(String code, String message) {
	        super(code,message);
	}
	
    public ParamErrorException(String code, String errorData, String message) {
        super(code,errorData,message);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}
