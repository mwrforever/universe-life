package com.universe.life.rabbitmq.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ 基础消息模型
 * <p>
 * 统一的消息基类，提供消息的基础属性和功能：
 * 1. 消息唯一标识
 * 2. 消息时间戳
 * 3. 消息优先级
 * 4. 消息重试机制
 * 5. 消息扩展属性
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Data
@Accessors(chain = true)
public class RabbitMqBaseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息类型（用于消息路由和处理）
     */
    private String messageType;

    /**
     * 消息优先级（0-9，数字越大优先级越高）
     */
    private Integer priority = 0;

    /**
     * 消息过期时间（毫秒）
     */
    private Long ttl;

    /**
     * 消息重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 消息来源服务
     */
    private String sourceService;

    /**
     * 目标服务
     */
    private String targetService;

    /**
     * 消息版本号
     */
    private String version = "1.0";

    /**
     * 业务数据
     */
    private Object payload;

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;

    /**
     * 构造函数
     */
    public RabbitMqBaseMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
        this.extensions = new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param messageType 消息类型
     * @param payload     业务数据
     */
    public RabbitMqBaseMessage(String messageType, Object payload) {
        this();
        this.messageType = messageType;
        this.payload = payload;
    }

    /**
     * 增加重试次数
     *
     * @return 当前对象
     */
    public RabbitMqBaseMessage incrementRetryCount() {
        this.retryCount++;
        return this;
    }

    /**
     * 检查是否还能重试
     *
     * @return 是否还能重试
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }

    /**
     * 添加扩展属性
     *
     * @param key   属性键
     * @param value 属性值
     * @return 当前对象
     */
    public RabbitMqBaseMessage addExtension(String key, Object value) {
        if (this.extensions == null) {
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
        return this;
    }

    /**
     * 获取扩展属性
     *
     * @param key 属性键
     * @param <T> 属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key) {
        if (this.extensions == null) {
            return null;
        }
        return (T) this.extensions.get(key);
    }

    @Override
    public String toString() {
        return "RabbitMqBaseMessage{" +
                "messageId='" + messageId + '\'' +
                ", createTime=" + createTime +
                ", messageType='" + messageType + '\'' +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", retryCount=" + retryCount +
                ", maxRetryCount=" + maxRetryCount +
                ", sourceService='" + sourceService + '\'' +
                ", targetService='" + targetService + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}