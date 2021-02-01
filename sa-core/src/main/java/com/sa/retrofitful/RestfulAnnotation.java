package com.sa.retrofitful;

import java.util.HashMap;
import java.util.Map;


public class RestfulAnnotation {
    private Object voBody;
    private Map<String, Object> voFields = new HashMap<>(4);
    private Map<String, String> headers;
    private Map<String, String> reqParams = new HashMap<>(4);
    private String post;
    private String get;

    public Object getVoBody() {
        return voBody;
    }

    public void setVoBody(Object voBody) {
        this.voBody = voBody;
    }

    public Map<String, Object> getVoFields() {
        return voFields;
    }

    public void setVoFields(Map<String, Object> voFields) {
        this.voFields = voFields;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getReqParams() {
        return reqParams;
    }

    public void setReqParams(Map<String, String> reqParams) {
        this.reqParams = reqParams;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getGet() {
        return get;
    }

    public void setGet(String get) {
        this.get = get;
    }
}
