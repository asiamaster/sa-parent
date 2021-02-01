package com.sa.dto;


public interface IBaseDomain extends IDomain<Long> {

    @Override
    Long getId();
    @Override
    void setId(Long id);
}
