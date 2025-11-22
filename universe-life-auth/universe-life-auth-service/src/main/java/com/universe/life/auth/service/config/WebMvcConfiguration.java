package com.universe.life.auth.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Web MVC配置
 * 确保静态资源能正确访问
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理器
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/html/**")
                .addResourceLocations("classpath:/static/html/");

        registry.addResourceHandler("/webfonts/**")
                .addResourceLocations("classpath:/static/webfonts/");

        // 配置favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public ResourceHttpRequestHandler defaultResourceRequestHandler() {
        return new ResourceHttpRequestHandler();
    }
}