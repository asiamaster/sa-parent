package com.sa.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.sa.dao.mapper.CommonMapper;
import com.sa.dto.IDTO;
import com.sa.metadata.ValuePair;
import com.sa.service.CommonService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class CommonServiceImpl implements CommonService {

	@Autowired
	private CommonMapper commonMapper;

	@Override
	public List<ValuePair<?>> selectValuePair(String sql) {
		return commonMapper.selectValuePair(sql);
	}

	@Override
	public List<JSONObject> selectJSONObject(String sql, Integer page, Integer rows) {

		PageHelper.startPage(page, rows);
		return commonMapper.selectJSONObject(sql);
	}

	@Override
	public List<Map> selectMap(String sql, Integer page, Integer rows) {

		PageHelper.startPage(page, rows);
		return commonMapper.selectMap(sql);
	}

	@Override
	public <T extends IDTO> List<T> selectDto(String sql, Class<T> resultType, Integer page, Integer rows) {

		PageHelper.startPage(page, rows);
		return commonMapper.selectDto(sql, resultType);
	}

	@Override
	public <T extends IDTO> List<T> selectDto(String sql, Class<T> resultType) {
		return commonMapper.selectDto(sql, resultType);
	}

	@Override
	public void execute(String sql) {
		commonMapper.execute(sql);
	}

}
