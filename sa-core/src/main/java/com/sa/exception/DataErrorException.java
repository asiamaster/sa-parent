
package com.sa.exception;


import com.sa.constant.ResultCode;


public class DataErrorException extends AppException{
	private static final long serialVersionUID = 178901230987541001L;
	public DataErrorException() {
		super();
		this.message = "数据错误!";
		this.code = ResultCode.DATA_ERROR;
	}
	
	public DataErrorException(String message) {
		super(message);
		this.code = ResultCode.DATA_ERROR;
	}
	
	public DataErrorException(String message, Throwable cause) {
		super(message, cause);
		this.code = ResultCode.DATA_ERROR;
	}
	
	public DataErrorException(Throwable cause) {
		super(cause);
		this.code = ResultCode.DATA_ERROR;
	}
	
	public DataErrorException(String code, String errorData, String message) {
        super(code,errorData,message);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
