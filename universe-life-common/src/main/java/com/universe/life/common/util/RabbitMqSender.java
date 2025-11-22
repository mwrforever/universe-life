package com.universe.life.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 基于MessageConfig的统一RabbitMQ消息发送工具类
 * <p>
 * 通过MessageConfig统一管理所有消息属性，提供简洁的消息发送接口
 * </p>
 *
 * @author universe-life
 * @version 1.0.0
 * @since 2024-11-11
 */
@Slf4j
@RequiredArgsConstructor
public final class RabbitMqSender {

    private final RabbitTemplate rabbitTemplate;
    private final Executor executor;


    /**
     * 开始构建消息发送配置
     *
     * @return MessageBuilder 消息构建器
     */
    public MessageBuilder builder() {
        return new MessageBuilder(this);
    }


    /**
     * 核心消息发送方法
     */
    private void sendMessage(MessageConfig config, Object message) {
        try {
            log.debug("发送消息 - 配置: {}, 消息类型: {}", config, message.getClass().getSimpleName());
            // 创建CorrelationData，使用UUID生成并去除-
            CorrelationData cd = new CorrelationData(UUID.randomUUID().toString().replace("-", ""));

            MessagePostProcessor processor = createMessagePostProcessor(config);
            rabbitTemplate.convertAndSend(config.exchange, config.routingKey, message, processor, cd);
            log.info("消息发送成功 - 配置: {}, CorrelationData ID: {}", config, cd.getId());

        } catch (Exception e) {
            log.error("消息发送失败 - 配置: {}, 错误: {}", config, e.getMessage(), e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 核心异步消息发送方法
     */
    private CompletableFuture<Void> sendMessageAsync(MessageConfig config, Object message) {
        return CompletableFuture.runAsync(() -> sendMessage(config, message), executor)
                .exceptionally(throwable -> {
                    log.error("异步消息发送失败 - 配置: {}, 错误: {}", config, throwable.getMessage(), throwable);
                    throw new RuntimeException("异步消息发送失败", throwable);
                });
    }

    /**
     * 创建消息后处理器
     */
    private MessagePostProcessor createMessagePostProcessor(MessageConfig config) {
        return msg -> {
            // 设置TTL
            if (config.ttlMillis != null) {
                msg.getMessageProperties().setExpiration(String.valueOf(config.ttlMillis));
            }

            // 设置持久化
            if (config.persistent != null && config.persistent) {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            }

            // 设置头部信息
            if (config.headers != null && !config.headers.isEmpty()) {
                config.headers.forEach((key, value) -> msg.getMessageProperties().setHeader(key, value));
            }

            // 设置时间戳
            msg.getMessageProperties().setTimestamp(new Date(System.currentTimeMillis()));

            return msg;
        };
    }


    /**
     * 消息配置类，封装所有消息属性
     */
    private static class MessageConfig {
        final String exchange;
        final String routingKey;
        final Long ttlMillis;
        final Boolean persistent;
        final Map<String, Object> headers;

        MessageConfig(MessageBuilder builder) {
            this.exchange = builder.exchange;
            this.routingKey = builder.routingKey;
            this.ttlMillis = builder.ttlMillis;
            this.persistent = builder.persistent;
            this.headers = builder.headers;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("MessageConfig{");

            if (exchange != null) sb.append("exchange='").append(exchange).append('\'');
            if (routingKey != null) {
                if (sb.length() > 14) sb.append(", ");
                sb.append("routingKey='").append(routingKey).append('\'');
            }
            if (ttlMillis != null) {
                if (sb.length() > 14) sb.append(", ");
                sb.append("ttl=").append(ttlMillis).append("ms");
            }
            if (persistent != null) {
                if (sb.length() > 14) sb.append(", ");
                sb.append("persistent=").append(persistent);
            }
            if (headers != null && !headers.isEmpty()) {
                if (sb.length() > 14) sb.append(", ");
                sb.append("headers=").append(headers.size());
            }

            sb.append("}");
            return sb.toString();
        }
    }


    /**
     * MessageConfig构建器，统一管理消息配置
     */
    public static class MessageBuilder {
        private final RabbitMqSender sender;
        private String exchange;
        private String routingKey;
        private Long ttlMillis;
        private Boolean persistent;
        private Map<String, Object> headers;

        private MessageBuilder(RabbitMqSender sender) {
            this.sender = sender;
        }

        /**
         * 设置目标交换机和路由键
         *
         * @param exchange   交换机名称
         * @param routingKey 路由键
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder to(String exchange, String routingKey) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            return this;
        }

        /**
         * 设置目标路由键（使用默认交换机）
         *
         * @param routingKey 路由键
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder to(String routingKey) {
            return to(null, routingKey);
        }

        /**
         * 设置TTL（使用Duration）
         *
         * @param ttl TTL时间
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder ttl(Duration ttl) {
            return ttl(ttl.toMillis());
        }

        /**
         * 设置TTL（使用毫秒）
         *
         * @param ttlMillis TTL时间，单位毫秒
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder ttl(long ttlMillis) {
            this.ttlMillis = ttlMillis;
            return this;
        }

        /**
         * 设置持久化属性
         *
         * @param persistent 是否持久化
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder persistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        /**
         * 添加单个头部信息
         *
         * @param name  头部信息名称
         * @param value 头部信息值
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder header(String name, Object value) {
            if (this.headers == null) {
                this.headers = new java.util.HashMap<>();
            }
            this.headers.put(name, value);
            return this;
        }

        /**
         * 设置多个头部信息
         *
         * @param headers 头部信息Map
         * @return MessageBuilder 构建器实例
         */
        public MessageBuilder headers(Map<String, Object> headers) {
            if (headers != null && !headers.isEmpty()) {
                if (this.headers == null) {
                    this.headers = new java.util.HashMap<>();
                }
                this.headers.putAll(headers);
            }
            return this;
        }


        /**
         * 发送消息
         *
         * @param message 消息内容
         */
        public void send(Object message) {
            sender.sendMessage(buildConfig(), message);
        }

        /**
         * 异步发送消息
         *
         * @param message 消息内容
         * @return CompletableFuture<Void> 异步操作结果
         */
        public CompletableFuture<Void> sendAsync(Object message) {
            return sender.sendMessageAsync(buildConfig(), message);
        }

        /**
         * 批量异步发送消息
         *
         * @param messages 消息列表
         * @return CompletableFuture<Void> 所有消息发送完成的异步结果
         */
        public CompletableFuture<Void> sendBatchAsync(Object... messages) {
            if (messages == null || messages.length == 0) {
                return CompletableFuture.completedFuture(null);
            }

            MessageConfig config = buildConfig();

            return CompletableFuture.allOf(
                    Arrays.stream(messages)
                            .map(message -> sender.sendMessageAsync(config, message))
                            .toArray(CompletableFuture[]::new)
            ).whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("批量消息发送成功 - 配置: {}, 消息数量: {}", config, messages.length);
                } else {
                    log.error("批量消息发送部分失败 - 配置: {}, 消息数量: {}", config, messages.length, throwable);
                }
            });
        }

        /**
         * 构建消息配置
         *
         * @return MessageConfig 消息配置
         */
        private MessageConfig buildConfig() {
            return new MessageConfig(this);
        }
    }
}