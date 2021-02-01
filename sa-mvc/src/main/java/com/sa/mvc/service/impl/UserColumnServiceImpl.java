package com.sa.mvc.service.impl;

import com.sa.mvc.domain.UserColumn;
import com.sa.mvc.service.UserColumnService;
import com.sa.redis.service.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Service;


@Service
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class UserColumnServiceImpl implements UserColumnService {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	public Environment env;

	public static final String USER_COLUMN_KEY_PREFIX = "user_columns_";

	@Override
	public void saveUserColumns(UserColumn userColumn) {
		if(StringUtils.isBlank(userColumn.getSystem())){
			userColumn.setSystem(env.getProperty("spring.application.name"));
		}
		String tableKey = USER_COLUMN_KEY_PREFIX+userColumn.getUserId();
		String fieldKey = USER_COLUMN_KEY_PREFIX+userColumn.getSystem()+"_"+userColumn.getModule()+"_"+userColumn.getNamespace();
		BoundHashOperations<String, String, String[]> boundHashOperations =
				redisUtil.getRedisTemplate().boundHashOps(tableKey);
		boundHashOperations.delete(fieldKey);
		boundHashOperations.put(fieldKey, userColumn.getColumns());
	}

	@Override
	public String[] getUserColumns(UserColumn userColumn) {
		if(StringUtils.isBlank(userColumn.getSystem())){
			userColumn.setSystem(env.getProperty("spring.application.name"));
		}
		String tableKey = USER_COLUMN_KEY_PREFIX+userColumn.getUserId();
		String fieldKey = USER_COLUMN_KEY_PREFIX+userColumn.getSystem()+"_"+userColumn.getModule()+"_"+userColumn.getNamespace();
		return (String[])redisUtil.getRedisTemplate().boundHashOps(tableKey).get(fieldKey);
	}

}
