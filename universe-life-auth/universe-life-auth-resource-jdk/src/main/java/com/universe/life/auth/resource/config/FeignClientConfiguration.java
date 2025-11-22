package com.universe.life.auth.resource.config;

import com.universe.life.auth.resource.interceptors.FeignRequestInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 毛伟然
 * @since 2025/11/19 14:04
 */
@Configuration
public class FeignClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(RequestInterceptor.class)
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor();
    }

}
