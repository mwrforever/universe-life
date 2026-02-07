package com.universe.life.auth.resource.config;

import com.universe.life.auth.resource.util.PermissionMatcher;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author 毛伟然
 * @since 2025/12/4 10:09
 */
@Configuration
public class AuthUtilConfiguration {


    @Bean
    public VerifyCaptchaUtil verifyCaptchaIssuerUtil(
            StringRedisTemplate stringRedisTemplate
    ) {
        return new VerifyCaptchaUtil(stringRedisTemplate);
    }


    @Bean("pm")
    public PermissionMatcher permissionMatcher() {
        return new PermissionMatcher();
    }

}
