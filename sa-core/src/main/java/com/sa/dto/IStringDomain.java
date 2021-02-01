package com.sa.dto;


public interface IStringDomain extends IDomain<String> {
    @Override
    String getId();
    @Override
    void setId(String id);
}
