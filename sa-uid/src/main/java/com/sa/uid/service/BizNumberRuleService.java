package com.sa.uid.service;

import com.sa.base.BaseService;
import com.sa.domain.BaseOutput;
import com.sa.uid.domain.BizNumberRule;


public interface BizNumberRuleService extends BaseService<BizNumberRule, Long> {

    BizNumberRule getByType(String type);


    BaseOutput updateEnable(Long id, Boolean enable);
}