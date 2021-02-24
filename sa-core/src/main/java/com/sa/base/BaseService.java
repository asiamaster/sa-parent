package com.sa.base;

import com.sa.domain.BasePage;
import com.sa.domain.EasyuiPageOutput;

import java.io.Serializable;
import java.util.List;



public interface BaseService<T,KEY extends Serializable> {


	int insert(T t);


	int insertSelective(T t);
	

	int batchInsert(List<T> list);
	

	int delete(KEY key);


	int deleteByExample(T t);


	int delete(List<KEY> keys);
	

	int update(T condtion);


	int updateSelective(T condtion);


	int updateByExample(T domain, T condition);


	int batchUpdateSelective(List<T> list);


	int updateSelectiveByExample(T domain, T condition);


	int updateExactByExample(T domain, T condition);


	int updateExactByExampleSimple(T domain, T condition);


	int updateExact(T record);


	int updateExactSimple(T record);


	int batchUpdate(List<T> list);
	

	int saveOrUpdate(T t);


	int saveOrUpdateSelective(T t);


	T get(KEY key);


	List<T> list(T condtion);
	

	BasePage<T> listPage(T t);


	List<T> selectByExample(Object example);

	T selectByPrimaryKey(KEY key);

	List<T> listByExample(T domain);


	BasePage<T> listPageByExample(T domain);


	EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) throws Exception;


	EasyuiPageOutput listEasyuiPageByExample(T domain, boolean useProvider) throws Exception;


	boolean existsWithPrimaryKey(KEY key);


	int insertExact(T t);


	int insertExactSimple(T t);
}
