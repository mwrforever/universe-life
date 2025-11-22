package com.universe.life.common.config;

import com.universe.life.common.properties.MailProperties;
import com.universe.life.common.strategy.CaptchaSenderStrategy;
import com.universe.life.common.util.MailCaptchaSender;
import com.universe.life.common.util.PhoneCaptchaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

/**
 * @author 毛伟然
 * @since 2025/11/16 15:05
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MailProperties.class)
public class CaptchaConfiguration {


    @Bean
    public CaptchaSenderStrategy mailCaptchaSender(
            MailProperties mailProperties,
            JavaMailSender mailSender,
            TemplateEngine templateEngine
    ) {
        return new MailCaptchaSender(mailSender, templateEngine, mailProperties);
    }

    @Bean
    public CaptchaSenderStrategy phoneCaptchaSender() {
        return new PhoneCaptchaSender();
    }

}
