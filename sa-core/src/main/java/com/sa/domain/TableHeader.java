package com.sa.domain;


public class TableHeader {

    private String field;

    private String title;

    private String type;

    private String format;

    public TableHeader(String field, String title) {
        this.field = field;
        this.title = title;
    }

    public TableHeader(String field, String title, String type, String format) {
        this.field = field;
        this.title = title;
        this.type = type;
        this.format = format;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
