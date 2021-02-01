package com.sa.dto;


import com.sa.exception.InternalException;


public class DTOProxyException extends InternalException {
	private static final long serialVersionUID = -88899901616112234L;


	public DTOProxyException(String message) {
		super(message);
	}


	public DTOProxyException(String message, Throwable cause) {
		super(message, cause);
	}

}
