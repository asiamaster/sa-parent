package com.sa.oplog.dto;

import java.util.Map;


public class LogContext {
    private String remoteHost;
    private int serverPort;
    private String requestURI;
    private String requestURL;
    private Map<String, String[]> parameterMap;

    private Map<String, Object> bizInfo;

    private Object user;

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public Map<String, Object> getBizInfo() {
        return bizInfo;
    }

    public void setBizInfo(Map<String, Object> bizInfo) {
        this.bizInfo = bizInfo;
    }
}
