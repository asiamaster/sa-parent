package com.sa.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sa.domain.BaseOutput;
import com.sa.quartz.domain.QuartzConstants;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.quartz.domain.ScheduleMessage;
import com.sa.util.AopTargetUtils;
import com.sa.util.OkHttpUtils;
import com.sa.util.SpringUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * http任务工具
 */
public class TaskUtils {
	public final static Logger log = LoggerFactory.getLogger(TaskUtils.class);

	static Map<String, Object> cachedJobHashMap = new HashMap<>();

	public static boolean invokeMethod(ScheduleJob scheduleJob, ScheduleMessage scheduleMessage) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		if(null == scheduleJob || null == scheduleMessage){
			return true;
		}
		String timesKey = scheduleJob.getJobGroup()+scheduleJob.getJobName();
		if(!QuartzConstants.sheduelTimes.containsKey(timesKey)){
			QuartzConstants.sheduelTimes.put(timesKey,0);
		}
		QuartzConstants.sheduelTimes.put(timesKey,QuartzConstants.sheduelTimes.get(timesKey)+1);
		scheduleMessage.setSheduelTimes(QuartzConstants.sheduelTimes.get(timesKey));
		scheduleMessage.setJobGroup(scheduleJob.getJobGroup());
		scheduleMessage.setJobName(scheduleJob.getJobName());
		Object object = null;
		if (StringUtils.isNotBlank(scheduleJob.getSpringId())) {
			object = SpringUtil.getBean(scheduleJob.getSpringId());
			return invokeLocalMethod(scheduleJob, scheduleMessage, object);
		} else if (StringUtils.isNotBlank(scheduleJob.getBeanClass())) {
			if(!cachedJobHashMap.containsKey(scheduleJob.getSpringId())){
				Class clazz = Class.forName(scheduleJob.getBeanClass());
				cachedJobHashMap.put(scheduleJob.getSpringId(), clazz.newInstance());
			}
			object = cachedJobHashMap.get(scheduleJob.getBeanClass());
			return invokeLocalMethod(scheduleJob, scheduleMessage, object);
		}else if(StringUtils.isNotBlank(scheduleJob.getUrl())){
			if(scheduleJob.getIsConcurrent()!= null && scheduleJob.getIsConcurrent()==1){
				return asyncExecute(scheduleJob.getUrl(), JSONObject.toJSONString(scheduleMessage), "POST");
			}else{
				return syncExecute(scheduleJob.getUrl(), JSONObject.toJSONString(scheduleMessage), "POST");
			}
		}else{
			log.info("无可调度的对象!");
			return true;
		}
	}

	private static boolean invokeLocalMethod(ScheduleJob scheduleJob, ScheduleMessage scheduleMessage, Object object){
		if (object == null) {
			log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查是否配置正确！！！");
			return false;
		}
		Method method = null;
		Object targetObj = null;
		try {
			targetObj = AopTargetUtils.getTarget(object);
			method = targetObj.getClass().getDeclaredMethod(scheduleJob.getMethodName(), ScheduleMessage.class);
			if (method != null && targetObj != null) {
				Object result = method.invoke(targetObj, scheduleMessage);
				if(Boolean.FALSE.equals(result)){
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			String message = e.getMessage();
			if(e instanceof InvocationTargetException){
				message = ((InvocationTargetException) e).getTargetException().getMessage();
			}
			log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，调度参数设置错误！异常:" + message);
			return false;
		}
	}

	/**
	 * 同步远程调用
	 * @param url
	 * @param paramObj
	 * @param httpMethod
	 * @return
	 */
	private static boolean syncExecute(String url, Object paramObj, String httpMethod){
		try{
			Map<String, String> headersMap = new HashMap<>(1);
			headersMap.put("Content-Type", "application/json;charset=utf-8");
			String bodyString;
			if("POST".equalsIgnoreCase(httpMethod)){
				String json = paramObj instanceof String ? (String)paramObj : JSON.toJSONString(paramObj);
				bodyString = OkHttpUtils.postBodyString(url, json, headersMap, null);
			}else{
				bodyString = OkHttpUtils.get(url, (Map)JSON.toJSON(paramObj), headersMap, null);
			}

			if(StringUtils.isNotBlank(bodyString)){
				if("false".equalsIgnoreCase(bodyString)){
					return false;
				}
				if(!StringUtils.equalsIgnoreCase("200", JSONObject.parseObject(bodyString, BaseOutput.class, Feature.IgnoreNotMatch).getCode())){
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			log.error(String.format("远程调用["+url+"]发生异常, message:[%s]", e.getMessage()));
			return false;
		}
	}

	/**
	 * 异步远程调用
	 * @param url
	 * @param paramObj
	 * @param httpMethod
	 * @return
	 */
	private static boolean asyncExecute(String url, Object paramObj, String httpMethod){
		try{
			Map<String, String> headersMap = new HashMap<>(1);
			headersMap.put("Content-Type", "application/json;charset=utf-8");
			if("POST".equalsIgnoreCase(httpMethod)){

				String json = paramObj instanceof String ? (String)paramObj : JSON.toJSONString(paramObj);
				OkHttpUtils.postBodyStringAsync(url, json, headersMap, null, new Callback() {

					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {

					}
				});
			}else{
				OkHttpUtils.getAsync(url, (Map)JSON.toJSON(paramObj), headersMap, null, new Callback() {

					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {

					}
				});
			}
			log.info("异步远程调用["+url+"]完成");
			return true;
		} catch (Exception e) {
			log.error(String.format("异步远程调用["+url+"]发生异常,message:[%s]", e.getMessage()));
			return false;
		}
	}
}
