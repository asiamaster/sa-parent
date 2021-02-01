package com.sa.domain;

import java.util.List;
import java.util.Map;


public class ExportParam {

    private String title ="EXPORT";


    private List<List<Map<String, Object>>> columns;


    private Map<String, String> queryParams;


    private String url;


    private String contentType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<List<Map<String, Object>>> getColumns() {
        return columns;
    }

    public void setColumns(List<List<Map<String, Object>>> columns) {
        this.columns = columns;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
