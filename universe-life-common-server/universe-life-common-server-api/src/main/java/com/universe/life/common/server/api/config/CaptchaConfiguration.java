package com.universe.life.common.server.api.config;

import com.universe.life.common.properties.MailProperties;
import com.universe.life.common.server.api.strategy.CaptchaSenderStrategy;
import com.universe.life.common.server.api.util.MailCaptchaSender;
import com.universe.life.common.server.api.util.PhoneCaptchaSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

/**
 * 验证码配置
 *
 * @author 毛伟然
 * @since 2025/11/16 15:05
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class CaptchaConfiguration {


    /**
     * 邮件验证码发送器
     * 只有在 JavaMailSender Bean 存在时才创建
     */
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
