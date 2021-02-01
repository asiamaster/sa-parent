package com.sa.mvc.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@ConditionalOnExpression("'${CSRFInterceptor.enable}'=='true'")
@ConfigurationProperties(prefix="web.CSRFInterceptor", ignoreInvalidFields=true)
public class CSRFInterceptorProperties {


	@Value("${enable:false}")
	private Boolean enable;
	@Value("${paths:/**}")
	private List<String> paths = new ArrayList<>();
	@Value("${excludePaths:/resources/**}")
	private List<String> excludePaths = new ArrayList<>();


	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public List<String> getExcludePaths() {
		return excludePaths;
	}

	public void setExcludePaths(List<String> excludePaths) {
		this.excludePaths = excludePaths;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
}
