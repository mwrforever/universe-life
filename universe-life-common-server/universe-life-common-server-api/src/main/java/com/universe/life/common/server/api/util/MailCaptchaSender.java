package com.universe.life.common.server.api.util;

import cn.hutool.core.lang.Validator;
import com.universe.life.common.properties.MailProperties;
import com.universe.life.common.server.api.strategy.CaptchaSenderStrategy;
import com.universe.life.common.server.model.domain.domain.dto.MailCaptchaDTO;
import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;

/**
 * 邮箱验证码发送工具类
 * 支持HTML格式的验证码邮件发送
 *
 * @author 毛伟然
 * @since 2025/11/16 15:02
 */
@Slf4j
@RequiredArgsConstructor
public class MailCaptchaSender implements CaptchaSenderStrategy {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;


    @Override
    public boolean support(String identification) {
        return Validator.isEmail(identification);
    }

    @Override
    public void send(String identification, String captcha) {
        // 默认使用登录类型的验证码
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail(identification)
                .setCaptcha(captcha)
                .setUsageType(CaptchaUsageType.LOGIN)
                .setPlatformUrl(mailProperties.getAppUrl());

        sendHtmlCaptcha(captchaDTO);
    }

    @Override
    public void send(String identification, String captcha, CaptchaUsageType usageType) {
        sendHtmlCaptcha(createCaptchaDTO(identification, captcha, usageType));
    }

    /**
     * 发送HTML格式的验证码邮件
     *
     * @param captchaDTO 验证码邮件数据传输对象
     */
    public void sendHtmlCaptcha(MailCaptchaDTO captchaDTO) {
        try {
            if (!Validator.isEmail(captchaDTO.getToEmail())) {
                throw new IllegalArgumentException("邮箱地址格式不正确: " + captchaDTO.getToEmail());
            }

            // 创建MimeMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 设置邮件基本信息
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(captchaDTO.getToEmail());
            helper.setSubject(generateSubject(captchaDTO));
            helper.setSentDate(new Date());

            // 生成HTML内容
            String htmlContent = generateHtmlContent(captchaDTO);
            helper.setText(htmlContent, true);

            // 发送邮件
            mailSender.send(mimeMessage);

            log.info("验证码邮件发送成功 - 收件人: {}, 验证码: {}, 用途: {}",
                    captchaDTO.getToEmail(),
                    captchaDTO.getCaptcha(),
                    captchaDTO.getUsageType() != null ? captchaDTO.getUsageType().getDisplayName() : "未知");

        } catch (Exception e) {
            log.error("验证码邮件发送失败 - 收件人: {}, 错误: {}",
                    captchaDTO.getToEmail(), e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成邮件主题
     */
    private String generateSubject(MailCaptchaDTO captchaDTO) {
        if (captchaDTO.getCustomSubject() != null && !captchaDTO.getCustomSubject().trim().isEmpty()) {
            return captchaDTO.getCustomSubject();
        }

        String usageName = captchaDTO.getUsageType() != null ?
                captchaDTO.getUsageType().getDescription() : "验证";
        return String.format("[%s] %s", mailProperties.getAppName(), usageName);
    }

    /**
     * 生成HTML邮件内容
     */
    private String generateHtmlContent(MailCaptchaDTO captchaDTO) {
        if (captchaDTO.getCustomContent() != null && !captchaDTO.getCustomContent().trim().isEmpty()) {
            return captchaDTO.getCustomContent();
        }

        Context context = new Context();
        context.setVariable("captcha", captchaDTO.getCaptcha());
        context.setVariable("usageType", captchaDTO.getUsageType());
        context.setVariable("expirationMinutes", captchaDTO.getExpirationMinutes());
        context.setVariable("platformUrl", captchaDTO.getPlatformUrl());

        return templateEngine.process("mail/captcha-notification", context);
    }

    /**
     * 创建验证码DTO的便捷方法
     *
     * @param toEmail   目标邮箱
     * @param captcha   验证码
     * @param usageType 使用类型
     * @return 验证码DTO
     */
    public MailCaptchaDTO createCaptchaDTO(String toEmail, String captcha, CaptchaUsageType usageType) {
        return new MailCaptchaDTO()
                .setToEmail(toEmail)
                .setCaptcha(captcha)
                .setUsageType(usageType)
                .setPlatformUrl(mailProperties.getAppUrl());
    }
}
