package com.csu.be.forum.config;

import com.csu.be.forum.controller.interceptor.LoginRequiredInterceptor;
import com.csu.be.forum.controller.interceptor.LoginTicketInterceptor;
import com.csu.be.forum.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/26 1:36
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                    .excludePathPatterns("/static/**");
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/static/**");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/static/**");
    }
}
