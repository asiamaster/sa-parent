package com.sa.mvc.controller;

import com.alibaba.fastjson.JSONObject;
import com.sa.domain.BaseOutput;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Controller("error")
@RequestMapping("/error")
public class MainsiteErrorController implements ErrorController {

	protected static final Logger log = LoggerFactory.getLogger(MainsiteErrorController.class);
	private static final String ERROR_PATH = "/error";
	@Autowired
	private ErrorAttributes errorAttributes;


	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public BaseOutput<String> ajaxError(WebRequest request, HttpServletResponse response){


		BaseOutput baseOutput = buildBody(request,true);
		log.error(JSONObject.toJSONString(baseOutput));
		return baseOutput;
	}


	@RequestMapping(produces = "text/html")
	public String handleError(HttpServletRequest request, HttpServletResponse response){

		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if(statusCode == 401){
			return SpringUtil.getProperty("error.page.404", "error/noAccess");
		}







		return SpringUtil.getProperty("error.page.404", "error/404");
	}


	@RequestMapping("/noLogin.do")
	public String noLogin(HttpServletRequest request, HttpServletResponse response){
		return SpringUtil.getProperty("error.page.noLogin", "error/noLogin");
	}


	@RequestMapping("/noAccess.do")
	public String noAccess(HttpServletRequest request, HttpServletResponse response){
		return SpringUtil.getProperty("error.page.noAccess", "error/noAccess");
	}


	@Override
	public String getErrorPath() {
		return ERROR_PATH+"/default";
	}


	private Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {

		return errorAttributes.getErrorAttributes(request, includeStackTrace);
	}


	private BaseOutput buildBody(WebRequest request,Boolean includeStackTrace){
		Map<String,Object> errorAttributes = getErrorAttributes(request, includeStackTrace);
		Integer status=(Integer)errorAttributes.get("status");
		String path=(String)errorAttributes.get("path");
		String messageFound=(String)errorAttributes.get("message");
		String message="";
		String trace ="";
		if(!StringUtils.isEmpty(path)){
			message=String.format("Requested path %s with result %s",path,messageFound);
		}
		if(includeStackTrace) {
			trace = (String) errorAttributes.get("trace");
			if(!StringUtils.isEmpty(trace)) {
				message += String.format(" and trace %s", trace);
			}
		}
		return BaseOutput.failure(message).setCode(status.toString());
	}
}
