package com.sa.domain;

import com.sa.dto.IBaseDomain;


public class BaseDomain extends Domain<Long> implements IBaseDomain {

    @Override
    public Long getId() {
        return id;
    }
    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
