package com.sa.mvc.servlet;

import com.alibaba.fastjson.JSONObject;
import com.sa.constant.ResultCode;
import com.sa.domain.BaseOutput;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component("CSRFInterceptor")
public class CSRFInterceptor extends HandlerInterceptorAdapter {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



		if(!(handler instanceof HandlerMethod)) return true;

		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();
		VerifyCSRFToken verifyCSRFToken = method.getAnnotation(VerifyCSRFToken.class);

		if (verifyCSRFToken != null) {

			String xrq = request.getHeader("X-Requested-With");

			if (verifyCSRFToken.verify() && !verifyCSRFToken(request)) {
				if (StringUtils.isEmpty(xrq)) {

					String csrftoken = CSRFTokenUtil.generate(request);
					request.getSession().setAttribute("CSRFToken", csrftoken);
					response.setContentType("application/json;charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.print("非法请求");
					response.flushBuffer();
					return false;
				} else {

					String csrftoken = CSRFTokenUtil.generate(request);
					request.getSession().setAttribute("CSRFToken", csrftoken);
					response.setContentType("application/json;charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.print(JSONObject.toJSONString(new BaseOutput(ResultCode.CSRF_ERROR,"无效的token，或者token过期")));
					response.flushBuffer();
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {

		if (modelAndView != null) {
			if (request.getSession(false) == null || StringUtils.isEmpty((String) request.getSession(false).getAttribute("CSRFToken"))) {
				request.getSession().setAttribute("CSRFToken", CSRFTokenUtil.generate(request));
				return;
			}
		}
		if(!(handler instanceof HandlerMethod)) return;

		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();
		RefreshCSRFToken refreshAnnotation = method.getAnnotation(RefreshCSRFToken.class);

		String xrq = request.getHeader("X-Requested-With");
		if (refreshAnnotation != null && refreshAnnotation.refresh() && StringUtils.isEmpty(xrq)) {
			request.getSession().setAttribute("CSRFToken", CSRFTokenUtil.generate(request));
			return;
		}

		VerifyCSRFToken verifyAnnotation = method.getAnnotation(VerifyCSRFToken.class);
		if (verifyAnnotation != null) {
			if (verifyAnnotation.verify()) {
				if (StringUtils.isEmpty(xrq)) {
					request.getSession().setAttribute("CSRFToken", CSRFTokenUtil.generate(request));
				} else {
					Map<String, String> map = new HashMap<String, String>();
					String token = CSRFTokenUtil.generate(request);
					request.getSession().setAttribute("CSRFToken", token);
					map.put("CSRFToken", token);
					response.setContentType("application/json;charset=UTF-8");
					OutputStream out = response.getOutputStream();
					out.write((",'csrf':" + JSONObject.toJSONString(map) + "}").getBytes("UTF-8"));
				}
			}
		}
	}
	
	protected boolean verifyCSRFToken(HttpServletRequest request) {

		String requstCSRFToken = request.getHeader("CSRFToken");
		if (StringUtils.isEmpty(requstCSRFToken)) {
			return false;
		}
		String sessionCSRFToken = (String) request.getSession().getAttribute("CSRFToken");
		if (StringUtils.isEmpty(sessionCSRFToken)) {
			return false;
		}
		return requstCSRFToken.equals(sessionCSRFToken);
	}
}
