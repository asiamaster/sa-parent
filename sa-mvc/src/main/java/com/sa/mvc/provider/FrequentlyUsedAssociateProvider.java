package com.sa.mvc.provider;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValuePairImpl;
import com.sa.metadata.ValueProvider;
import com.sa.metadata.provider.ValueComparator;
import com.sa.redis.service.RedisUtil;
import com.sa.service.CommonService;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;


@Component("fuaProvider")
@ConditionalOnExpression("'${fuaProvider.enable}'=='true'")
@ConfigurationProperties(prefix = "fuaProvider")
public class FrequentlyUsedAssociateProvider implements ApplicationListener<ContextRefreshedEvent>, ValueProvider {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FrequentlyUsedAssociateProvider.class);

	private final static int concurrencyLevel = 8;

	private final static int initialCapacity = 10;

	private final static int maximumSize = 100;


	private final static int FIRST_HIT_SAVE_TIMES = 3;

	private final static int MAX_HIT_SAVE_TIMES = 10;

	private final static int MAX_ASSOCIATE_COUNT = 10;

	private int corePoolSize = 2;

	private int maximumPoolSize = 4;

	private int keepAliveTime = 30;

	private final static String MODEL_KEY = "model";
	private Map<String, LoadingCache<String, Integer>> cacheMap = new HashMap<>(8);
	private static final String QUERY_PARAMS_KEY = "queryParams";

	private ThreadPoolExecutor executor;


	private List<String> models;

	private String persistenceType = "redis";



	@Autowired
	private CommonService commonService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10000));
		executor.allowCoreThreadTimeOut(true);
		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				LOGGER.error("["+this.getClass().getSimpleName()+"]消息处理线程池：线程池已满，无法接收处理-新任务：" + r.toString());
				if (!executor.isShutdown()) {
					Thread th = new Thread(r);
					th.start();
				}
			}
		});



			if(models == null || models.isEmpty()) {
				return;
			}
			for (String key : models) {
				initByKey(key);
			}

	}

	@Override
	public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
		if(null == val || StringUtils.isBlank(val.toString())) {
			return null;
		}
		List<ValuePair<?>> buffer = new ArrayList<ValuePair<?>>();
		JSONObject params = JSONObject.parseObject(metaMap.get(QUERY_PARAMS_KEY).toString());
		String key = params.getString(MODEL_KEY);
		if(StringUtils.isBlank(key)) {
			return null;
		}
		ConcurrentMap<String, Integer> loadingCacheMap = cacheMap.get(key).asMap();
		TreeSet<String> treeSet = new TreeSet<>(new ValueComparator(loadingCacheMap));

		for (Map.Entry<String, Integer> entry : loadingCacheMap.entrySet()) {
			if(entry.getKey().contains(val.toString())){
				treeSet.add(entry.getKey());
			}
		}

		int treeSetIndex = 0;

		for(String value : treeSet){
			buffer.add(new ValuePairImpl(value, value));
			treeSetIndex++;
			if(treeSetIndex > MAX_ASSOCIATE_COUNT) {
				break;
			}
		}
		return buffer;
	}

	
	public void save(String key, String value){
		if(StringUtils.isBlank(value)) {
			return;
		}

		Integer hitTimes = cacheMap.get(key).getUnchecked(value);

		if (hitTimes.equals(FIRST_HIT_SAVE_TIMES) || hitTimes % MAX_HIT_SAVE_TIMES == 0) {
			persist(key, value, hitTimes);
		}
		cacheMap.get(key).refresh(value);
	}

	@Override
	public String getDisplayText(Object val, Map metaMap, FieldMeta fieldMeta) {
		return null;
	}


	private void initByKey(String key) {
		cacheMap.put(key, initCache(key));
	}


	private LoadingCache<String, Integer> initCache(String key) {


		LoadingCache<String, Integer> loadingCache = CacheBuilder.newBuilder()

				.concurrencyLevel(concurrencyLevel)



				.initialCapacity(initialCapacity)

				.maximumSize(maximumSize)








				.build(
						new CacheLoader<String, Integer>() {
							@Override
							public Integer load(String key) throws Exception {
								return 1;
							}

							@Override
							public ListenableFuture<Integer> reload(String key, Integer oldValue) throws Exception {
								checkNotNull(key);
								checkNotNull(oldValue);
								return Futures.immediateFuture(oldValue + 1);
							}
						}
				);
		initFromDb(loadingCache, key);
		return loadingCache;
	}


	private void initFromDb(LoadingCache<String, Integer> loadingCache, String key) {
		if ("redis".equals(persistenceType)) {

			if(!getRedisUtil().getRedisTemplate().hasKey(key)) {
				return;
			}
			Map<String, Integer> map = getRedisUtil().getRedisTemplate().boundHashOps(key).entries();
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				loadingCache.put(entry.getKey(), entry.getValue());
			}
		}else{
			StringBuilder createTableSql = new StringBuilder("create table if not exists ")
					.append(key)
					.append("\n(\n")
					.append("   id                   bigint not null auto_increment,\n")
					.append("   name                 varchar(120) not null comment '联想字段',\n")
					.append("   value                int comment '出现次数',\n")
					.append("   primary key (id),\n")
					.append("   unique key AK_uk_name (name)\n")
					.append(")");

			commonService.execute(createTableSql.toString());
			List<JSONObject> list = commonService.selectJSONObject("select name, value from " + key, 1, Integer.MAX_VALUE);
			for(JSONObject jo : list) {
				loadingCache.put(jo.getString("name"), jo.getInteger("value"));
			}
		}
	}


	private void persist(String key, String value, Integer hitTimes) {
		LoadingCache<String, Integer> loadingCache = cacheMap.get(key);
		executor.execute(() ->{
			if ("redis".equals(persistenceType)) {





				getRedisUtil().getRedisTemplate().boundHashOps(key).put(value, hitTimes);
			} else {





				StringBuilder sql = new StringBuilder("replace into `").append(key).append("` (`name`, `value`) values ('").append(value).append("', ").append(hitTimes).append(")");

				commonService.execute(sql.toString());
			}
		});
	}

	public List<String> getModels() {
		return models;
	}

	public void setModels(List<String> models) {
		this.models = models;
	}

	public String getPersistenceType() {
		return persistenceType;
	}

	public void setPersistenceType(String persistenceType) {
		this.persistenceType = persistenceType;
	}


	private RedisUtil getRedisUtil() {
		return SpringUtil.getBean(RedisUtil.class);
	}

}
