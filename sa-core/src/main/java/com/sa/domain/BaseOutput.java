
package com.sa.domain;


import com.sa.constant.ResultCode;


public class BaseOutput<T> {

    
    private String code;
    
    private String message;
    
    @Deprecated
    private String result;
    
    private T data;
    
    private Object metadata;
    
    private String errorData;

    public BaseOutput() {
    }

    public BaseOutput(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public BaseOutput setCode(String code) {
        this.code = code;
        return this;
    }

    public Object getMetadata() {
        return metadata;
    }

    public BaseOutput setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public BaseOutput setMessage(String message) {
        this.message = message;
        return this;
    }

    
    @Deprecated
    public String getResult() {
        return result == null ? getMessage() : result;
    }
    @Deprecated
    public BaseOutput setResult(String result) {
        this.result = result;
        this.message = result;
        return this;
    }

    public T getData() {
        return (T) data;
    }

    public BaseOutput setData(T data) {
        this.data = data;
        return this;
    }

    public static <T> BaseOutput<T> successData(T data) {
        return success().setData(data);
    }

    public static <T> BaseOutput<T> create(String code, String result) {
        return new BaseOutput<T>(code, result);
    }

    public static <T> BaseOutput<T> success() {
        return success("OK");
    }

    public static <T> BaseOutput<T> success(String msg) {
        return create(ResultCode.OK, msg);
    }

    public static <T> BaseOutput<T> failure() {
        return failure("操作失败!");
    }

    public static <T> BaseOutput<T> failure(String msg) {
        return create(ResultCode.APP_ERROR, msg);
    }

    public static <T> BaseOutput<T> failure(String code, String msg) {
        return create(code, msg);
    }

    public String getErrorData() {
        return errorData;
    }

    public BaseOutput setErrorData(String errorData) {
        this.errorData = errorData;
        return this;
    }

    public boolean isSuccess(){
        return ResultCode.OK.equals(this.code);
    }
}
