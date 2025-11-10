package com.universe.life.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ配置属性类
 * <p>
 * 提供对RabbitMQ高级功能的配置支持，通过YAML配置文件自动装配
 * 支持队列、交换机、死信队列、延迟队列等配置
 *
 * @author universe-life
 * @version 1.0.0
 * @since 2024-11-10
 */
@Data
@Component
@ConfigurationProperties(prefix = "universe.rabbitmq")
public class RabbitMqConfigProperties {

    /**
     * 队列配置
     */
    private Map<String, QueueConfig> queues;

    /**
     * 交换机配置
     */
    private Map<String, ExchangeConfig> exchanges;

    /**
     * 死信队列配置
     */
    private DeadLetterConfig deadLetter;

    /**
     * 延迟队列配置
     */
    private DelayConfig delay;

    /**
     * 默认配置
     */
    private DefaultConfig defaultConfig = new DefaultConfig();

    /**
     * 队列配置
     */
    @Data
    public static class QueueConfig {
        /**
         * 队列名称
         */
        private String name;

        /**
         * 是否持久化
         */
        private Boolean durable = true;

        /**
         * 是否独占
         */
        private Boolean exclusive = false;

        /**
         * 是否自动删除
         */
        private Boolean autoDelete = false;

        /**
         * 队列参数
         */
        private Map<String, Object> arguments;
    }

    /**
     * 交换机配置
     */
    @Data
    public static class ExchangeConfig {
        /**
         * 交换机名称
         */
        private String name;

        /**
         * 交换机类型
         */
        private String type;

        /**
         * 是否持久化
         */
        private Boolean durable = true;

        /**
         * 是否自动删除
         */
        private Boolean autoDelete = false;

        /**
         * 交换机参数
         */
        private Map<String, Object> arguments;
    }

    /**
     * 死信队列配置
     */
    @Data
    public static class DeadLetterConfig {
        /**
         * 主队列名称
         */
        private String mainQueue;

        /**
         * 死信队列名称
         */
        private String deadLetterQueue;

        /**
         * 死信交换机名称
         */
        private String deadLetterExchange;

        /**
         * 死信路由键
         */
        private String deadLetterRoutingKey;
    }

    /**
     * 延迟队列配置
     */
    @Data
    public static class DelayConfig {
        /**
         * 延迟队列名称
         */
        private String delayQueue;

        /**
         * 目标队列名称
         */
        private String targetQueue;

        /**
         * 延迟交换机名称
         */
        private String delayExchange;

        /**
         * 目标交换机名称
         */
        private String targetExchange;

        /**
         * 路由键
         */
        private String routingKey;

        /**
         * 延迟时间（毫秒）
         */
        private Long delayTtl;
    }

    /**
     * 默认配置
     */
    @Data
    public static class DefaultConfig {
        /**
         * 默认重试次数
         */
        private Integer maxRetryAttempts = 3;

        /**
         * 默认重试间隔（毫秒）
         */
        private Long retryInterval = 1000L;

        /**
         * 默认TTL（毫秒）
         */
        private Long defaultTtl = 60000L; // 60秒

        /**
         * 默认预取数量
         */
        private Integer defaultPrefetch = 1;

        /**
         * 默认并发消费者数
         */
        private Integer defaultConcurrency = 1;

        /**
         * 最大并发消费者数
         */
        private Integer maxConcurrency = 5;
    }
}