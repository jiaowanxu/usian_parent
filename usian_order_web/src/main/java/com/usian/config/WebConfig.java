package com.usian.config;

import com.usian.interceptor.UserLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfigurer:往springmvc容器中注入拦截器、日期转换器
 * 拦截器配置类
 */
@Component
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserLoginInterceptor userLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(userLoginInterceptor);
        //拦截那个url
        interceptorRegistration.addPathPatterns("/frontend/order/**");
    }
}
