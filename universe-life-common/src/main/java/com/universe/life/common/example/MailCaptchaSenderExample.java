package com.universe.life.common.example;

import com.universe.life.common.domain.dto.MailCaptchaDTO;
import com.universe.life.common.enums.CaptchaUsageType;
import com.universe.life.common.util.MailCaptchaSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MailCaptchaSender 使用示例
 * 展示如何在业务代码中使用邮箱验证码发送功能
 *
 * @author 毛伟然
 * @since 2025/11/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailCaptchaSenderExample {

    private final MailCaptchaSender mailCaptchaSender;

    /**
     * 示例1: 发送登录验证码（使用默认配置）
     *
     * @param email 用户邮箱
     * @param captcha 验证码
     */
    public void sendLoginCaptcha(String email, String captcha) {
        try {
            // 方法1: 使用接口默认实现（自动识别为登录类型）
            mailCaptchaSender.send(email, captcha);

            log.info("登录验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("登录验证码发送失败: {}", email, e);
            throw e;
        }
    }

    /**
     * 示例2: 发送注册验证码
     *
     * @param email 用户邮箱
     * @param captcha 验证码
     */
    public void sendRegisterCaptcha(String email, String captcha) {
        try {
            // 方法2: 使用便捷方法创建DTO
            MailCaptchaDTO captchaDTO = mailCaptchaSender.createCaptchaDTO(
                    email, captcha, CaptchaUsageType.REGISTER);

            mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

            log.info("注册验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("注册验证码发送失败: {}", email, e);
            throw e;
        }
    }

    /**
     * 示例3: 发送密码重置验证码（自定义配置）
     *
     * @param email 用户邮箱
     * @param captcha 验证码
     */
    public void sendPasswordResetCaptcha(String email, String captcha) {
        try {
            // 方法3: 手动创建DTO并设置自定义参数
            MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                    .setToEmail(email)
                    .setCaptcha(captcha)
                    .setUsageType(CaptchaUsageType.RESET_PASSWORD)
                    .setExpirationMinutes(10) // 10分钟有效期
                    .setCustomSubject("[Universe Life] 密码重置验证码")
                    .setPlatformUrl("http://127.0.0.1:3000/reset-password");

            mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

            log.info("密码重置验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("密码重置验证码发送失败: {}", email, e);
            throw e;
        }
    }

    /**
     * 示例4: 发送完全自定义的HTML邮件
     *
     * @param email 目标邮箱
     * @param subject 邮件主题
     */
    public void sendCustomWelcomeEmail(String email, String subject) {
        try {
            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>欢迎加入 Universe Life</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }
                            .container { max-width: 600px; margin: 0 auto; }
                            .header { background: #667eea; color: white; padding: 20px; text-align: center; }
                            .content { padding: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>🌌 Universe Life</h1>
                            </div>
                            <div class="content">
                                <h2>欢迎加入我们！</h2>
                                <p>感谢您注册 Universe Life 平台，开始您的探索之旅！</p>
                                <p>
                                    <a href="http://127.0.0.1:3000"
                                       style="background: #667eea; color: white; padding: 10px 20px;
                                              text-decoration: none; border-radius: 5px;">
                                        立即开始使用
                                    </a>
                                </p>
                                <p>如有任何问题，请随时联系我们的客服团队。</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            mailCaptchaSender.sendCustomHtmlEmail(email, subject, htmlContent);

            log.info("欢迎邮件发送成功: {}", email);
        } catch (Exception e) {
            log.error("欢迎邮件发送失败: {}", email, e);
            throw e;
        }
    }

    /**
     * 示例5: 批量发送不同类型的验证码
     *
     * @param email 用户邮箱
     */
    public void sendMultipleCaptchaTypes(String email) {
        try {
            // 发送登录验证码
            MailCaptchaDTO loginCaptcha = new MailCaptchaDTO()
                    .setToEmail(email)
                    .setCaptcha("123456")
                    .setUsageType(CaptchaUsageType.LOGIN);
            mailCaptchaSender.sendHtmlCaptcha(loginCaptcha);

            // 发送注册验证码
            MailCaptchaDTO registerCaptcha = new MailCaptchaDTO()
                    .setToEmail(email)
                    .setCaptcha("789012")
                    .setUsageType(CaptchaUsageType.REGISTER);
            mailCaptchaSender.sendHtmlCaptcha(registerCaptcha);

            // 发送绑定邮箱验证码
            MailCaptchaDTO bindEmailCaptcha = new MailCaptchaDTO()
                    .setToEmail(email)
                    .setCaptcha("345678")
                    .setUsageType(CaptchaUsageType.BIND_EMAIL)
                    .setExpirationMinutes(15);
            mailCaptchaSender.sendHtmlCaptcha(bindEmailCaptcha);

            log.info("批量验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("批量验证码发送失败: {}", email, e);
            throw e;
        }
    }

    /**
     * 示例6: 使用自定义HTML内容
     *
     * @param email 用户邮箱
     * @param captcha 验证码
     */
    public void sendCaptchaWithCustomTemplate(String email, String captcha) {
        try {
            String customHtmlTemplate = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                        <div style="max-width: 500px; margin: 0 auto; border: 1px solid #ddd; padding: 20px;">
                            <h2 style="color: #667eea;">🌌 Universe Life</h2>
                            <h3>您的验证码</h3>
                            <div style="background: #f8f9fa; padding: 15px; margin: 20px 0; border-radius: 5px;">
                                <span style="font-size: 24px; font-weight: bold; letter-spacing: 5px;">%s</span>
                            </div>
                            <p style="color: #6c757d;">此验证码5分钟内有效，请勿泄露给他人。</p>
                            <p><a href="http://127.0.0.1:3000" style="color: #667eea;">访问 Universe Life</a></p>
                        </div>
                    </body>
                    </html>
                    """, captcha);

            MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                    .setToEmail(email)
                    .setCaptcha(captcha)
                    .setUsageType(CaptchaUsageType.LOGIN)
                    .setCustomContent(customHtmlTemplate);

            mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

            log.info("自定义模板验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("自定义模板验证码发送失败: {}", email, e);
            throw e;
        }
    }
}