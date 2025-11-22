package com.universe.life.common.domain.dto;

import com.universe.life.common.enums.CaptchaUsageType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 邮件验证码数据传输对象
 *
 * @author 毛伟然
 * @since 2025/11/17
 */
@Data
@Accessors(chain = true)
public class MailCaptchaDTO {

    /**
     * 目标邮箱地址
     */
    private String toEmail;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码使用类型
     */
    private CaptchaUsageType usageType;

    /**
     * 自定义邮件主题，如果不提供则使用默认主题
     */
    private String customSubject;

    /**
     * 自定义邮件内容，如果不提供则使用默认模板
     */
    private String customContent;

    /**
     * 平台官网链接
     */
    private String platformUrl = "http://127.0.0.1:3000";

    /**
     * 验证码过期时间（分钟）
     */
    private Integer expirationMinutes = 5;
}