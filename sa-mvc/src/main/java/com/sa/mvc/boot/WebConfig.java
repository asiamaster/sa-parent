package com.sa.mvc.boot;

import com.sa.mvc.converter.JsonHttpMessageConverter;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@ConditionalOnExpression("'${web.enable}'=='true'")

public class WebConfig implements WebMvcConfigurer {

	@Value("${web.instanceResolver:false}")
	private Boolean instanceResolver;
	@Autowired
	public Environment env;

	@Bean
	public Converter<String, Date> addDateConvert() {
		return new Converter<String, Date>() {
			@Override
			public Date convert(String source) {
				if(StringUtils.isBlank(source)){
					return null;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					date = sdf.parse(source);
				} catch (ParseException e) {
					sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						date = sdf.parse(source);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				return date;
			}
		};
	}

	@Bean
	public Converter<String, LocalDate> addLocalDateConvert() {
		return new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(String source) {
				return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}
		};
	}

	@Bean
	public Converter<String, LocalDateTime> addLocalDateTimeConvert() {
		return new Converter<String, LocalDateTime>() {
			@Override
			public LocalDateTime convert(String source) {
				return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			}
		};
	}

	@Bean
	public Converter<String, Instant> addInstantConvert() {
		return new Converter<String, Instant>() {
			@Override
			public Instant convert(String source) {
				try {

					return Instant.ofEpochMilli(Long.parseLong(source));
				} catch (NumberFormatException e) {

					return Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).parse(source));
				}
			}
		};
	}








	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		Iterator it =  converters.iterator();
		int index = -1;
		int tmp = 0;
		while (it.hasNext()){
			Object obj = it.next();

			if(obj instanceof MappingJackson2HttpMessageConverter ){
				it.remove();
				index = tmp;
			}
			if(index == -1) {
				tmp++;
			}
		}
		JsonHttpMessageConverter fastJsonHttpMessageConverter = new JsonHttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		supportedMediaTypes.add(MediaType.TEXT_PLAIN);
		fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);





		if(index != -1){
			converters.add(index, fastJsonHttpMessageConverter);
		}

		else {
			converters.add(fastJsonHttpMessageConverter);
		}
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		if(instanceResolver){
			argumentResolvers.add(new DTOInstArgumentResolver());
		}else {
			argumentResolvers.add(new DTOArgumentResolver());
		}
	}














	@Bean
	@ConditionalOnExpression("'${exceptionResolver.enable}'=='true'")
	public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
		SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();

		simpleMappingExceptionResolver.setDefaultErrorView("error/default");

		simpleMappingExceptionResolver.setExceptionAttribute("exception");

		Properties mappings = new Properties();
		mappings.put("java.lang.RuntimeException", SpringUtil.getProperty("error.page.default", "error/default"));
		mappings.put("java.lang.Exception", SpringUtil.getProperty("error.page.default", "error/default"));
		mappings.put("java.lang.Throwable", SpringUtil.getProperty("error.page.default", "error/default"));
		simpleMappingExceptionResolver.setExceptionMappings(mappings);
		return simpleMappingExceptionResolver;
	}


	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(10);

		executor.setMaxPoolSize(20);

		executor.setQueueCapacity(200);

		executor.setKeepAliveSeconds(60);

		executor.setThreadNamePrefix("taskExecutor-");

		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		executor.setWaitForTasksToCompleteOnShutdown(true);

		executor.setAwaitTerminationSeconds(60);
	}






	public Boolean getInstanceResolver() {
		return instanceResolver;
	}

	public void setInstanceResolver(Boolean instanceResolver) {
		this.instanceResolver = instanceResolver;
	}
}
