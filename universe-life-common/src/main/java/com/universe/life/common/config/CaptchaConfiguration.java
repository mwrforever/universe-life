package com.universe.life.common.config;

import com.universe.life.common.strategy.CaptchaSenderStrategy;
import com.universe.life.common.util.EmailCaptchaSender;
import com.universe.life.common.util.PhoneCaptchaSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 毛伟然
 * @since 2025/11/16 15:05
 */
@Configuration
public class CaptchaConfiguration {

    @Bean
    public CaptchaSenderStrategy emailCaptchaSender() {
        return new EmailCaptchaSender();
    }

    @Bean
    public CaptchaSenderStrategy phoneCaptchaSender() {
        return new PhoneCaptchaSender();
    }

}
