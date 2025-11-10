package com.universe.life.rabbitmq.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 通知消息模型
 * <p>
 * 用于系统通知的消息传递，包括：
 * 1. 系统通知
 * 2. 邮件通知
 * 3. 短信通知
 * 4. 应用内通知
 * 5. 微信/钉钉等第三方通知
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NotificationMessage extends RabbitMqBaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 通知类型（EMAIL, SMS, PUSH, IN_APP, WECHAT, DINGTALK）
     */
    private String notificationType;

    /**
     * 通知主题
     */
    private String subject;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 接收者ID（用户ID）
     */
    private Long receiverId;

    /**
     * 接收者邮箱
     */
    private String receiverEmail;

    /**
     * 接收者手机号
     */
    private String receiverPhone;

    /**
     * 通知模板ID
     */
    private String templateId;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 通知优先级（1:低 2:中 3:高 4:紧急）
     */
    private Integer notificationPriority = 2;

    /**
     * 是否需要即时送达
     */
    private Boolean immediateDelivery = true;

    /**
     * 计划发送时间（用于延迟发送）
     */
    private java.time.LocalDateTime scheduledTime;

    /**
     * 通知渠道配置
     */
    private NotificationChannel channel;

    /**
     * 重试策略配置
     */
    private RetryStrategy retryStrategy;

    /**
     * 构造函数
     */
    public NotificationMessage() {
        super();
        this.setMessageType("NOTIFICATION_MESSAGE");
    }

    /**
     * 构造函数
     *
     * @param notificationType 通知类型
     * @param receiverId       接收者ID
     * @param subject          通知主题
     * @param content          通知内容
     */
    public NotificationMessage(String notificationType, Long receiverId, String subject, String content) {
        this();
        this.notificationType = notificationType;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
    }

    /**
     * 创建邮件通知消息
     *
     * @param receiverId   接收者ID
     * @param receiverEmail 接收者邮箱
     * @param subject      邮件主题
     * @param content      邮件内容
     * @return 通知消息
     */
    public static NotificationMessage createEmailMessage(Long receiverId, String receiverEmail,
                                                       String subject, String content) {
        return new NotificationMessage("EMAIL", receiverId, subject, content)
                .setReceiverEmail(receiverEmail)
                .addExtension("channel", "EMAIL");
    }

    /**
     * 创建短信通知消息
     *
     * @param receiverId   接收者ID
     * @param receiverPhone 接收者手机号
     * @param content      短信内容
     * @return 通知消息
     */
    public static NotificationMessage createSmsMessage(Long receiverId, String receiverPhone, String content) {
        return new NotificationMessage("SMS", receiverId, null, content)
                .setReceiverPhone(receiverPhone)
                .addExtension("channel", "SMS");
    }

    /**
     * 创建应用内推送消息
     *
     * @param receiverId 接收者ID
     * @param title      推送标题
     * @param content    推送内容
     * @return 通知消息
     */
    public static NotificationMessage createPushMessage(Long receiverId, String title, String content) {
        return new NotificationMessage("PUSH", receiverId, title, content)
                .addExtension("channel", "PUSH");
    }

    /**
     * 创建延迟通知消息
     *
     * @param notificationType 通知类型
     * @param receiverId       接收者ID
     * @param subject          通知主题
     * @param content          通知内容
     * @param scheduledTime    计划发送时间
     * @return 通知消息
     */
    public static NotificationMessage createDelayedMessage(String notificationType, Long receiverId,
                                                         String subject, String content,
                                                         java.time.LocalDateTime scheduledTime) {
        return new NotificationMessage(notificationType, receiverId, subject, content)
                .setScheduledTime(scheduledTime)
                .setImmediateDelivery(false);
    }

    /**
     * 创建高优先级紧急通知
     *
     * @param notificationType 通知类型
     * @param receiverId       接收者ID
     * @param subject          通知主题
     * @param content          通知内容
     * @return 通知消息
     */
    public static NotificationMessage createUrgentMessage(String notificationType, Long receiverId,
                                                         String subject, String content) {
        return new NotificationMessage(notificationType, receiverId, subject, content)
                .setNotificationPriority(4)
                .setImmediateDelivery(true)
                .addExtension("urgent", true);
    }

    /**
     * 通知渠道配置
     */
    @Data
    @Accessors(chain = true)
    public static class NotificationChannel {
        /**
         * 渠道类型
         */
        private String type;

        /**
         * 渠道配置参数
         */
        private Map<String, Object> config;

        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 渠道权重（用于负载均衡）
         */
        private Integer weight = 1;
    }

    /**
     * 重试策略配置
     */
    @Data
    @Accessors(chain = true)
    public static class RetryStrategy {
        /**
         * 最大重试次数
         */
        private Integer maxRetries = 3;

        /**
         * 重试间隔（秒）
         */
        private Long retryInterval = 60L;

        /**
         * 退避策略（FIXED, LINEAR, EXPONENTIAL）
         */
        private String backoffStrategy = "EXPONENTIAL";

        /**
         * 是否在失败时切换备用渠道
         */
        private Boolean switchChannelOnFailure = true;
    }
}