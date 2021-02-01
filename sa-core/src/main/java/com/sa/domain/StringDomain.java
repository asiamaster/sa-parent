package com.sa.domain;

import com.sa.dto.IStringDomain;


public class StringDomain extends Domain<String> implements IStringDomain {

    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
}
