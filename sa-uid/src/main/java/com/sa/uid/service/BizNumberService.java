package com.sa.uid.service;

import com.sa.base.BaseService;
import com.sa.uid.domain.BizNumber;
import com.sa.uid.domain.BizNumberRule;


public interface BizNumberService extends BaseService<BizNumber, Long> {


	void clear(String type);


	String getBizNumberByRule(BizNumberRule bizNumberRule);


	BizNumber selectOne(BizNumber bizNumber);
}