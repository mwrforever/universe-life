package com.universe.life.api.config;

import com.universe.life.api.fallback.UserClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 毛伟然
 * @since 2025/11/1 10:19
 */
@Configuration
public class FallbackConfiguration {

    @Bean
    public UserClientFallback userClientFallback() {
        return new UserClientFallback();
    }

}
