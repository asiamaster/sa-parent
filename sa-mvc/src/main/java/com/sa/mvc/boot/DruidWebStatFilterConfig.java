package com.sa.mvc.boot;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;






public class DruidWebStatFilterConfig {

    @Value("${loginUsername:admin}")
    private String loginUsername;

    @Value("${loginPassword:123456}")
    private String loginPassword;

    @Value("${resetEnable:true}")
    private String resetEnable;

    @Value("${allow:}")
    private String allow;

    @Value("${deny:}")
    private String deny;










    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/druid/*");


        if(StringUtils.isNoneBlank(allow)) {
            servletRegistrationBean.addInitParameter("allow", allow);
        }
        if(StringUtils.isNoneBlank(deny)) {

            servletRegistrationBean.addInitParameter("deny", deny);
        }

        servletRegistrationBean.addInitParameter("loginUsername",loginUsername);
        servletRegistrationBean.addInitParameter("loginPassword",loginPassword);

        servletRegistrationBean.addInitParameter("resetEnable",resetEnable);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean druidWebStatFilter() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(new WebStatFilter());
        reg.addUrlPatterns("/*");
        reg.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");
        reg.addInitParameter("sessionStatMaxCount","2000");
        reg.addInitParameter("sessionStatEnable","true");
        reg.addInitParameter("profileEnable","true");
        reg.addInitParameter("principalCookieName","SessionId");
        return reg;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getResetEnable() {
        return resetEnable;
    }

    public void setResetEnable(String resetEnable) {
        this.resetEnable = resetEnable;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getDeny() {
        return deny;
    }

    public void setDeny(String deny) {
        this.deny = deny;
    }
}
