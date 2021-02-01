package com.sa.uid.mapper;

import com.sa.base.MyMapper;
import com.sa.uid.domain.BizNumber;
import com.sa.uid.domain.BizNumberAndRule;

public interface BizNumberMapper extends MyMapper<BizNumber> {

    BizNumberAndRule getBizNumberAndRule(String type);
}