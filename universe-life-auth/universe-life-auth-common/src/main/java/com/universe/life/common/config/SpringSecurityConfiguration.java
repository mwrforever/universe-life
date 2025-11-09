package com.universe.life.common.config;

import com.universe.life.api.client.UserClient;
import com.universe.life.common.service.UserAuthInfoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author 毛伟然
 * @since 2025/11/4 14:20
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {

    @Bean
    public UserDetailsService userDetailsService(
            StringRedisTemplate stringRedisTemplate,
            UserClient userClient
    ) {
        return new UserAuthInfoService(userClient, stringRedisTemplate);
    }

}
