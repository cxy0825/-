package com.cxy.dp.config;

import com.cxy.dp.interceptor.LoginInterceptor;
import com.cxy.dp.interceptor.ReflushToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {


    @Bean
    public ReflushToken ReflushToken() {
        return new ReflushToken();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //默认情况下按照添加顺序进行拦截
        registry.addInterceptor(ReflushToken()).addPathPatterns("/**").order(0);
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                ).order(1);

    }


}
