package com.sa.base;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.sa.dao.ExampleExpand;
import com.sa.domain.BaseDomain;
import com.sa.domain.BasePage;
import com.sa.domain.EasyuiPageOutput;
import com.sa.domain.annotation.FindInSet;
import com.sa.domain.annotation.Like;
import com.sa.domain.annotation.Operator;
import com.sa.domain.annotation.SqlOperator;
import com.sa.dto.*;
import com.sa.exception.ParamErrorException;
import com.sa.metadata.ValueProviderUtils;
import com.sa.util.DateUtils;
import com.sa.util.POJOUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;



public abstract class BaseServiceAdaptor<T extends IDomain, KEY extends Serializable> implements BaseService<T, KEY> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceAdaptor.class);


	public abstract MyMapper<T> getDao();

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insert(T t) {
		return getDao().insert(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertSelective(T t) {
		return getDao().insertSelective(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchInsert(List<T> list) {
		return getDao().insertList(list);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int delete(KEY key) {
		return getDao().deleteByPrimaryKey(key);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteByExample(T t) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == t) {
			t = getDefaultBean (tClazz);
		}
		Example example = new Example(tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(t, example);
		}else {
			buildExampleByFields(t, example);
		}
		return getDao().deleteByExample(example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int delete(List<KEY> ids) {
		Type t = getClass().getGenericSuperclass();
		Class<T> entityClass = null;
		if(t instanceof ParameterizedType){
			Type[] p = ((ParameterizedType)t).getActualTypeArguments();
			entityClass = (Class<T>)p[0];
		}
		Example example = new Example(entityClass);
		example.createCriteria().andIn("id", ids);
		return getDao().deleteByExample(example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int updateSelective(T condtion) {
		return getDao().updateByPrimaryKeySelective(condtion);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int updateSelectiveByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExampleSelective(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int updateExactByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExampleExact(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int updateExactByExampleSimple(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {
			buildExampleByFields(condition, example);
		}
		try {
			buildExactDomain(domain, "setForceParams");
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		return getDao().updateByExampleExact(domain, example);
	}

	@Override

	public int updateExact(T record){
		return getDao().updateByPrimaryKeyExact(record);
	}

    @Override


    public int updateExactSimple(T record){
        try {
            buildExactDomain(record, "setForceParams");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return getDao().updateByPrimaryKeyExact(record);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int update(T condtion) {
		return getDao().updateByPrimaryKey(condtion);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)

	public int updateByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean (tClazz);
		}
		Example example = new Example(tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExample(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchUpdateSelective(List<T> list) {
		int count = 0;
		for(T t : list) {
			count+=getDao().updateByPrimaryKeySelective(t);
		}
		return count;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchUpdate(List<T> list) {
		int count = 0;
		for(T t : list) {
			count+=getDao().updateByPrimaryKey(t);
		}
		return count;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdate(T t) {
		KEY id = null;
		if (t instanceof IBaseDomain) {
			id = (KEY)((IBaseDomain) t).getId();
		} else {
			try {
				Class<?> clz = t.getClass();
				id = (KEY) clz.getMethod("getId").invoke(t);
			} catch (Exception e) {
				LOGGER.warn("获取对象主键值失败!");
			}
		}
		if(id != null) {
			return this.update(t);
		}
		return this.insert(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdateSelective(T t) {
		KEY id = null;
		if (t instanceof IBaseDomain) {
			id = (KEY)((IBaseDomain) t).getId();
		} else {
			try {
				Class<?> clz = t.getClass();
				id = (KEY) clz.getMethod("getId").invoke(t);
			} catch (Exception e) {
				LOGGER.warn("获取对象主键值失败!");
			}
		}
		if(id != null) {
			return this.updateSelective(t);
		}
		return this.insertSelective(t);
	}

	@Override

	public T get(KEY key) {
		return getDao().selectByPrimaryKey(key);
	}


	@Override
	public List<T> list(T condtion) {
		return getDao().select(condtion);
	}


	@Override
	public BasePage<T> listPage(T domain) {


		PageHelper.startPage(domain.getPage(), domain.getRows());
		List<T> list = getDao().select(domain);
		Page<T> page = (Page)list;
		BasePage<T> result = new BasePage<T>();
		result.setDatas(list);
		result.setPage(page.getPageNum());
		result.setRows(page.getPageSize());
		result.setTotalItem(Long.parseLong(String.valueOf(page.getTotal())));
		result.setTotalPage(page.getPages());
		result.setStartIndex(page.getStartRow());
		return result;
	}


	private T getDefaultBean(Class tClazz){
		T domain = null;
		if(tClazz.isInterface()){
			domain = DTOUtils.newDTO((Class<T>)tClazz);
		}else{
			try {
				domain = (T)tClazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		return domain;
	}


	public ExampleExpand getExample(T domain, Class<?> entityClass) {
		ExampleExpand exampleExpand = ExampleExpand.of(entityClass);
		if(!(domain instanceof IMybatisForceParams)){
			return exampleExpand;
		}
		IMybatisForceParams iMybatisForceParams =((IMybatisForceParams) domain);

		Set<String> selectColumns = iMybatisForceParams.getSelectColumns();
		if(selectColumns == null|| selectColumns.isEmpty()){
			return exampleExpand;
		}
		Boolean checkInjection = iMybatisForceParams.getCheckInjection();

		if (checkInjection == null || !checkInjection) {

			if(StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())){
				exampleExpand.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
			}
			try {
				Field selectColumnsField = Example.class.getDeclaredField("selectColumns");
				selectColumnsField.setAccessible(true);
				selectColumnsField.set(exampleExpand, selectColumns);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return exampleExpand;
		} else {
			ExampleExpand.Builder builder = new Example.Builder(entityClass);
			builder.select(selectColumns.toArray(new String[]{}));
			ExampleExpand exampleExpand1 = ExampleExpand.of(entityClass, builder);

			if(StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())){
				exampleExpand1.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
			}
			return exampleExpand1;
		}
	}

	@Override
	public List<T> listByExample(T domain){
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == domain) {
			domain = getDefaultBean (tClazz);
		}
		ExampleExpand example = getExample(domain, tClazz);

		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(domain, example);
		}else {
			buildExampleByFields(domain, example);
		}

		Integer page = domain.getPage();
		page = (page == null) ? Integer.valueOf(1) : page;
		if(domain.getRows() != null && domain.getRows() >= 1) {

			PageHelper.startPage(page, domain.getRows());
		}
		return getDao().selectByExampleExpand(example);
	}

	@Override
	public BasePage<T> listPageByExample(T domain){
		List<T> list = listByExample(domain);
		BasePage<T> result = new BasePage<T>();
		result.setDatas(list);
		if(list instanceof Page) {
			Page<T> page = (Page) list;
			result.setPage(page.getPageNum());
			result.setRows(page.getPageSize());
			result.setTotalItem(Long.parseLong(String.valueOf(page.getTotal())));
			result.setTotalPage(page.getPages());
			result.setStartIndex(page.getStartRow());
		}else{
			result.setPage(1);
			result.setRows(list.size());
			result.setTotalItem((long)list.size());
			result.setTotalPage(1);
			result.setStartIndex(1L);
		}
		return result;
	}

	@Override
	public List<T> selectByExample(Object example){
		return getDao().selectByExample(example);
	}

	@Override
	public boolean existsWithPrimaryKey(KEY key){
		return getDao().existsWithPrimaryKey(key);
	}

    @Override
    public int insertExact(T t){
        return getDao().insertExact(t);
    }

    @Override
    public int insertExactSimple(T t){
        try {
            buildExactDomain(t, "insertForceParams");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return getDao().insertExact(t);
    }


	@Override
	public EasyuiPageOutput listEasyuiPageByExample(T domain, boolean useProvider) throws Exception {
		List<T> list = listByExample(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}


	@Override
	public EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) throws Exception {
		if(domain.getRows() != null && domain.getRows() >= 1) {

			PageHelper.startPage(domain.getPage(), domain.getRows());
		}
		List<T> list = getDao().select(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}




    private void setOrderBy(T domain, Example example){

        if(StringUtils.isNotBlank(domain.getSort())) {
            StringBuilder orderByClauseBuilder = new StringBuilder();
            String[] sortFields = domain.getSort().split(",");
            String[] orderByTypes = domain.getOrder().split(",");

            if(sortFields.length > 1 && orderByTypes.length == 1){
				String orderByType = orderByTypes[0];
				orderByTypes = new String[sortFields.length];
            	for(int i=0; i<sortFields.length; i++){
					orderByTypes[i] = orderByType;
				}
			}

            for(int i=0; i < sortFields.length; i++) {
                String sortField = sortFields[i].trim();
                String orderByType = orderByTypes[i].trim();
                orderByType = StringUtils.isBlank(orderByType) ? "asc" : orderByType;
                orderByClauseBuilder.append("," + POJOUtils.humpToLineFast(sortField) + " " + orderByType);
            }
            if(orderByClauseBuilder.length()>1) {
                example.setOrderByClause(orderByClauseBuilder.substring(1));
            }
        }
    }


    protected void buildExampleByFields(T domain, Example example){
        Class tClazz = domain.getClass();

        if(tClazz.isInterface()) {
            return;
        }
        Example.Criteria criteria = example.createCriteria();

        parseNullField(domain, criteria);
        List<Field> fields = new ArrayList<>();
        getDeclaredField(domain.getClass(), fields);
        for(Field field : fields){
            Column column = field.getAnnotation(Column.class);
            String columnName = column == null ? field.getName() : column.name();

            if(isNullField(columnName, domain.getMetadata(IDTO.NULL_VALUE_FIELD))){
                continue;
            }
            Transient transient1 = field.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            Like like = field.getAnnotation(Like.class);
            Operator operator = field.getAnnotation(Operator.class);
			FindInSet findInSet = field.getAnnotation(FindInSet.class);

			SqlOperator sqlOperator = field.getAnnotation(SqlOperator.class);
            Class<?> fieldType = field.getType();
            Object value = null;
            try {
                field.setAccessible(true);
                value = field.get(domain);
                if(value instanceof Date){
                    value = DateFormatUtils.format((Date)value, "yyyy-MM-dd HH:mm:ss");
                }
            } catch (IllegalAccessException e) {
            }

            if(value == null) {
                continue;
            }

			if(value instanceof String && !checkXss((String)value)){
				throw new ParamErrorException("SQL注入拦截:"+value);
			}

			if(sqlOperator == null || SqlOperator.AND.equals(sqlOperator.value())) {
				if (like != null) {
					andLike(criteria, columnName, like.value(), value);
				} else if (operator != null) {
					if (!andOerator(criteria, columnName, fieldType, operator.value(), value)) {
						continue;
					}
				} else if (findInSet != null) {
					andFindInSet(criteria, columnName, value);
				} else {
					andEqual(criteria, columnName, value);
				}
			}else{
				if (like != null) {
					orLike(criteria, columnName, like.value(), value);
				} else if (operator != null) {
					if (!orOerator(criteria, columnName, fieldType, operator.value(), value)) {
						continue;
					}
				} else if (findInSet != null) {
					orFindInSet(criteria, columnName, value);
				} else {
					orEqual(criteria, columnName, value);
				}
			}
        }

		if(domain.getMetadata(IDTO.AND_CONDITION_EXPR) != null){
			criteria = criteria.andCondition(domain.getMetadata(IDTO.AND_CONDITION_EXPR).toString());
		}

		if(domain.getMetadata(IDTO.OR_CONDITION_EXPR) != null){
			criteria = criteria.orCondition(domain.getMetadata(IDTO.OR_CONDITION_EXPR).toString());
		}

        StringBuilder orderByClauseBuilder = new StringBuilder();
        for(Field field : tClazz.getFields()) {
            Transient transient1 = field.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            OrderBy orderBy = field.getAnnotation(OrderBy.class);
            if(orderBy == null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String columnName = column == null ? field.getName() : column.name();
            orderByClauseBuilder.append(","+columnName+" "+orderBy.value());
        }
        if(orderByClauseBuilder.length()>1) {
            example.setOrderByClause(orderByClauseBuilder.substring(1));
        }
        setOrderBy(domain, example);
    }

	private static final String sqlReg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
	private static Pattern sqlPattern = Pattern.compile(sqlReg, Pattern.CASE_INSENSITIVE);

	private boolean checkXss(String value) {
		if (value == null || "".equals(value)) {
			return true;
		}
		if (sqlPattern.matcher(value).find()) {
			LOGGER.error("SQL注入拦截:" + value);
			return false;
		}
		return true;
	}


    private boolean excludeMethod(Method method){

        if(!POJOUtils.isGetMethod(method)){
            return true;
        }
        if(method.getParameterTypes().length>0){
        	return true;
		}
        Class<?> declaringClass = method.getDeclaringClass();

        if (IBaseDomain.class.equals(declaringClass) || BaseDomain.class.equals(declaringClass)){
            return true;
        }
        return false;
    }


    protected void buildExampleByGetterMethods(T domain, Example example){
        Example.Criteria criteria = example.createCriteria();
        Class tClazz = DTOUtils.getDTOClass(domain);

        parseNullField(domain, criteria);
        List<Method> methods = new ArrayList<>();

        getDeclaredMethod(tClazz, methods);
        for(Method method : methods){
            if(excludeMethod(method)) {
                continue;
            }
            Column column = method.getAnnotation(Column.class);

            String columnName = column == null ? POJOUtils.humpToLineFast(POJOUtils.getBeanField(method)) : column.name();

            if(isNullField(columnName, domain.getMetadata(IDTO.NULL_VALUE_FIELD))){
                continue;
            }
            Transient transient1 = method.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            Like like = method.getAnnotation(Like.class);
			Operator operator = method.getAnnotation(Operator.class);
			FindInSet findInSet = method.getAnnotation(FindInSet.class);

			SqlOperator sqlOperator = method.getAnnotation(SqlOperator.class);
            Class<?> fieldType = method.getReturnType();
            Object value = getGetterValue(domain, method);

            if(value == null || "".equals(value)) {
                continue;
            }

			if(value instanceof String && !checkXss((String)value)){
				throw new ParamErrorException("SQL注入拦截:"+value);
			}
			if(sqlOperator == null || SqlOperator.AND.equals(sqlOperator.value())) {
				if (like != null) {
					andLike(criteria, columnName, like.value(), value);
				} else if (operator != null) {
					if (!andOerator(criteria, columnName, fieldType, operator.value(), value)) {
						continue;
					}
				} else if (findInSet != null) {
					andFindInSet(criteria, columnName, value);
				} else {
					andEqual(criteria, columnName, value);
				}
			}else{
				if (like != null) {
					orLike(criteria, columnName, like.value(), value);
				} else if (operator != null) {
					if (!orOerator(criteria, columnName, fieldType, operator.value(), value)) {
						continue;
					}
				} else if (findInSet != null) {
					orFindInSet(criteria, columnName, value);
				} else {
					orEqual(criteria, columnName, value);
				}
			}
        }

		if(domain.mget(IDTO.AND_CONDITION_EXPR) != null){
			criteria = criteria.andCondition(domain.mget(IDTO.AND_CONDITION_EXPR).toString());
		}

		if(domain.mget(IDTO.OR_CONDITION_EXPR) != null){
			criteria = criteria.orCondition(domain.mget(IDTO.OR_CONDITION_EXPR).toString());
		}

		buildOrderByClause(methods, example);

        setOrderBy(domain, example);
    }


	private void orLike(Example.Criteria criteria, String columnName, String likeValue, Object value){
		switch(likeValue){
			case Like.LEFT:
				criteria = criteria.orCondition(columnName + " like '%" + value + "' ");
				break;
			case Like.RIGHT:
				criteria = criteria.orCondition(columnName + " like '" + value + "%' ");
				break;
			case Like.BOTH:
				criteria = criteria.orCondition(columnName + " like '%" + value + "%' ");
				break;
			default : {
				if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
					criteria = criteria.orCondition(columnName + " = " + value + " ");
				}else{
					criteria = criteria.orCondition(columnName + " = '" + value + "' ");
				}
			}
		}
	}


	private boolean orOerator(Example.Criteria criteria, String columnName, Class<?> fieldType, String operatorValue, Object value){
		if(operatorValue.equals(Operator.IN) || operatorValue.equals(Operator.NOT_IN)){
			if(value instanceof Collection && CollectionUtils.isEmpty((Collection)value)){
				return false;
			}
			StringBuilder sb = new StringBuilder();
			if(Collection.class.isAssignableFrom(fieldType)){
				for(Object o : (Collection)value){
					if(o instanceof String){
						sb.append(", '").append(o).append("'");
					}else {
						sb.append(", ").append(o);
					}
				}
			}else if(fieldType.isArray()){
				for(Object o : ( (Object[])value)){
					if(o instanceof String){
						sb.append(", '").append(o).append("'");
					}else {
						sb.append(", ").append(o);
					}
				}
			}else{
				sb.append(", '").append(value).append("'");
			}
			criteria = criteria.orCondition(columnName + " " + operatorValue + "(" + sb.substring(1) + ")");
		}else {
			criteria = criteria.orCondition(columnName + " " + operatorValue + " '" + value + "' ");
		}
		return true;
	}


	private void orFindInSet(Example.Criteria criteria, String columnName, Object value){
		if(Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.orCondition("find_in_set (" + value + ", "+columnName+")");
		}else{
			criteria = criteria.orCondition("find_in_set ('" + value + "', "+columnName+")");
		}
	}


	private void orEqual(Example.Criteria criteria, String columnName, Object value){
		if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.orCondition(columnName + " = "+ value+" ");
		}else{
			criteria = criteria.orCondition(columnName + " = '"+ value+"' ");
		}
	}


	private void andEqual(Example.Criteria criteria, String columnName, Object value){
		if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.andCondition(columnName + " = "+ value+" ");
		}else{
			criteria = criteria.andCondition(columnName + " = '"+ value+"' ");
		}
	}


	private void andFindInSet(Example.Criteria criteria, String columnName, Object value){
		if(Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.andCondition("find_in_set (" + value + ", "+columnName+")");
		}else{
			criteria = criteria.andCondition("find_in_set ('" + value + "', "+columnName+")");
		}
	}


	private void andLike(Example.Criteria criteria, String columnName, String likeValue, Object value){
		switch(likeValue){
			case Like.LEFT:
				criteria = criteria.andCondition(columnName + " like '%" + value + "' ");
				break;
			case Like.RIGHT:
				criteria = criteria.andCondition(columnName + " like '" + value + "%' ");
				break;
			case Like.BOTH:
				criteria = criteria.andCondition(columnName + " like '%" + value + "%' ");
				break;
			default : {
				if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
					criteria = criteria.andCondition(columnName + " = " + value + " ");
				}else{
					criteria = criteria.andCondition(columnName + " = '" + value + "' ");
				}
			}
		}
	}


	private boolean andOerator(Example.Criteria criteria, String columnName, Class<?> fieldType, String operatorValue, Object value){
		if(operatorValue.equals(Operator.IN) || operatorValue.equals(Operator.NOT_IN)){
			if(value instanceof Collection && CollectionUtils.isEmpty((Collection)value)){
				return false;
			}
			StringBuilder sb = new StringBuilder();
			if(Collection.class.isAssignableFrom(fieldType)){
				for(Object o : (Collection)value){
					if(o instanceof String){
						sb.append(", '").append(o).append("'");
					}else {
						sb.append(", ").append(o);
					}
				}
			}else if(fieldType.isArray()){
				for(Object o : ( (Object[])value)){
					if(o instanceof String){
						sb.append(", '").append(o).append("'");
					}else {
						sb.append(", ").append(o);
					}
				}
			}else{
				sb.append(", '").append(value).append("'");
			}
			criteria = criteria.andCondition(columnName + " " + operatorValue + "(" + sb.substring(1) + ")");
		}else if(operatorValue.equals(Operator.BETWEEN) || operatorValue.equals(Operator.NOT_BETWEEN)){
			StringBuilder sb = new StringBuilder();
			if(List.class.isAssignableFrom(fieldType)){
				List list = (List)value;

				if((CollectionUtils.isEmpty(list) || list.size() != 2)){
					return false;
				}

				convertDatetimeList(list);
				if(list.get(0) instanceof String){
					sb.append("'").append(list.get(0)).append("' and '").append(list.get(1)).append("'");
				}else {
					sb.append(list.get(0)).append(" and ").append(list.get(1));
				}
			}else if(fieldType.isArray()){
				Object[] arrays = (Object[])value;

				if((arrays == null || arrays.length != 2)){
					return false;
				}

				arrays = convertDatetimeArray(arrays);
				sb = buildBetweenStringBuilderByArray(arrays);
			}else if(String.class.isAssignableFrom(fieldType)){
				String[] arrays = value.toString().split(",");

				if((arrays == null || arrays.length != 2)){
					return false;
				}
				sb = buildBetweenStringBuilderByArray(arrays);
			}else{
				return false;
			}
			sb.append(columnName).append(" ").append(operatorValue).append(sb);
			criteria = criteria.andCondition(sb.toString());
		}else {
			criteria = criteria.andCondition(columnName + " " + operatorValue + " '" + value + "' ");
		}
		return true;
	}


	private void convertDatetimeList(List list){
		String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
		if(list.get(0) instanceof Date){
			list.set(0, DateUtils.format((Date) list.get(0)));
			list.set(1, DateUtils.format((Date) list.get(1)));
		}else if(list.get(0) instanceof LocalDateTime){
			list.set(0, DateUtils.format((LocalDateTime)list.get(0), DATE_TIME));
			list.set(1, DateUtils.format((LocalDateTime)list.get(1), DATE_TIME));
		}else if(list.get(0) instanceof LocalDate){
			list.set(0, DateUtils.format(LocalDateTime.of((LocalDate) list.get(0), LocalTime.ofSecondOfDay(0)), DATE_TIME));
			list.set(1, DateUtils.format(LocalDateTime.of((LocalDate) list.get(1), LocalTime.ofSecondOfDay(0)), DATE_TIME));
		}
	}


	private Object[] convertDatetimeArray(Object[] objs){
		String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
		if(objs[0] instanceof Date){
			objs[0] = DateUtils.format((Date) objs[0]);
			objs[1] = DateUtils.format((Date) objs[1]);
		}else if(objs[0] instanceof LocalDateTime){
			objs[0] =  DateUtils.format((LocalDateTime)objs[0], DATE_TIME);
			objs[1] =  DateUtils.format((LocalDateTime)objs[1], DATE_TIME);
		}else if(objs[0] instanceof LocalDate){
			objs[0] = DateUtils.format(LocalDateTime.of((LocalDate) objs[0], LocalTime.ofSecondOfDay(0)), DATE_TIME);
			objs[1] = DateUtils.format(LocalDateTime.of((LocalDate) objs[1], LocalTime.ofSecondOfDay(0)), DATE_TIME);
		}
		return objs;
	}


	private StringBuilder buildBetweenStringBuilderByArray(Object[] arrays){
		StringBuilder sb = new StringBuilder(" ");
		if(arrays[0] instanceof String){
			sb.append("'").append(arrays[0]).append("' and '").append(arrays[1]).append("'");
		}else {
			sb.append(arrays[0]).append(" and ").append(arrays[1]);
		}
		return sb;
	}



	private void buildOrderByClause(List<Method> methods, Example example){
		StringBuilder orderByClauseBuilder = new StringBuilder();
		for(Method method : methods){
			Transient transient1 = method.getAnnotation(Transient.class);
			if(transient1 != null) {
				continue;
			}
			OrderBy orderBy = method.getAnnotation(OrderBy.class);
			if(orderBy == null) {
				continue;
			}
			Column column = method.getAnnotation(Column.class);
			String columnName = column == null ? POJOUtils.getBeanField(method) : column.name();
			orderByClauseBuilder.append(","+columnName+" "+orderBy.value());
		}
		if(orderByClauseBuilder.length()>1) {
			example.setOrderByClause(orderByClauseBuilder.substring(1));
		}
	}


	protected List<Field> getDeclaredField(Class clazz, List<Field> fields){
		List<Field> clazzFields = Lists.newArrayList(Arrays.copyOf(clazz.getDeclaredFields(), clazz.getDeclaredFields().length));

		for (Iterator<Field> it = clazzFields.iterator(); it.hasNext();) {
			Field clazzField = it.next();
			for(int i=0; i<fields.size(); i++) {
				if (fields.get(i).getName().equals(clazzField.getName())){
					it.remove();
				}
			}
		}
		fields.addAll(clazzFields);
		if(clazz.getSuperclass() != null){
			getDeclaredField(clazz.getSuperclass(), fields);
		}
		return fields;
	}


	protected List<Method> getDeclaredMethod(Class clazz, List<Method> methods){
		List<Method> clazzMethods = Lists.newArrayList(Arrays.copyOf(clazz.getDeclaredMethods(), clazz.getDeclaredMethods().length));

		for (Iterator<Method> it = clazzMethods.iterator(); it.hasNext();) {
			Method clazzMethod = it.next();
			for(int i=0; i<methods.size(); i++) {
				if (methods.get(i).getName().equals(clazzMethod.getName())){
					it.remove();
					break;
				}
			}
		}
		methods.addAll(clazzMethods);

		if(clazz.isInterface()) {
			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces != null) {
				for(Class<?> intf : interfaces) {
					getDeclaredMethod(intf, methods);
				}
			}
		}else {
			if (clazz.getSuperclass() != null) {
				getDeclaredMethod(clazz.getSuperclass(), methods);
			}
		}
		return methods;
	}


	private Class<Object> getSuperClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class) params[index];
	}


	private void buildExactDomain(T domain, String fieldName) throws Exception {

		if(!DTOUtils.isProxy(domain) && !DTOUtils.isInstance(domain)){
			return;
		}

		if(!IMybatisForceParams.class.isAssignableFrom(DTOUtils.getDTOClass(domain))){
			return;
		}
		Map params = new HashMap();

		Method[] dtoMethods = DTOUtils.getDTOClass(domain).getMethods();
		Map dtoMap = DTOUtils.go(domain);
		for(Method dtoMethod : dtoMethods){
			if(dtoMethod.getName().equals("getMetadata")){
				continue;
			}

			if(POJOUtils.isGetMethod(dtoMethod)){

				if(dtoMap.containsKey(POJOUtils.getBeanField(dtoMethod)) && dtoMethod.invoke(domain) == null){
                    Id id = dtoMethod.getAnnotation(Id.class);

                    if(id != null){
                        continue;
                    }
					Column column = dtoMethod.getAnnotation(Column.class);
					String columnName = column == null ? POJOUtils.humpToLine(POJOUtils.getBeanField(dtoMethod)) : column.name();
					params.put(columnName, null);
				}
			}
		}
		domain.aset(fieldName, params);
	}



	private Object getGetterValue(T domain, Method method){
		Object value = null;
		try {
			method.setAccessible(true);
			value = method.invoke(domain);
			if(value instanceof Date){
				value = DateFormatUtils.format((Date)value, "yyyy-MM-dd HH:mm:ss");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return value;
	}


	private boolean isNullField(String columnName, Object nullValueField){
		boolean isNullField = false;
		if(nullValueField != null){
			if(nullValueField instanceof String){
				if(columnName.equals(nullValueField)){
					isNullField = true;
				}
			}else if(nullValueField instanceof List){
				List<String> nullValueFields = (List)nullValueField;
				for(String field : nullValueFields){
					if(columnName.equals(field)){
						isNullField = true;
						break;
					}
				}
			}
		}
		return isNullField;
	}

	private void parseNullField(T domain, Example.Criteria criteria){

		Object nullValueField = DTOUtils.getDTOClass(domain).isInterface() ? domain.mget(IDTO.NULL_VALUE_FIELD) : domain.getMetadata(IDTO.NULL_VALUE_FIELD);
		if(nullValueField != null){
			if(nullValueField instanceof String){
				criteria = criteria.andCondition(nullValueField + " is null ");
			}else if(nullValueField instanceof List){
				List<String> nullValueFields = (List)nullValueField;
				for(String field : nullValueFields){
					criteria = criteria.andCondition(field + " is null ");
				}
			}
		}
	}

}