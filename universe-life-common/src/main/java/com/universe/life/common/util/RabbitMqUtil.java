package com.universe.life.common.util;

import cn.hutool.core.util.StrUtil;
import com.universe.life.common.config.RabbitMqConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * RabbitMQ工具类
 * <p>
 * 提供现代化的RabbitMQ操作工具，支持基础消息发送、高级队列操作、异步处理等功能
 * 集成Spring Boot自动配置，支持YAML配置文件驱动的队列和交换机管理
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础消息发送（同步/异步）</li>
 *   <li>队列和交换机的自动创建和管理</li>
 *   <li>延迟消息支持</li>
 *   <li>优先级队列支持</li>
 *   <li>死信队列支持</li>
 *   <li>消息TTL设置</li>
 *   <li>发送确认和返回回调</li>
 *   <li>批量消息处理</li>
 * </ul>
 *
 * @author universe-life
 * @version 1.0.0
 * @since 2024-11-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitMqUtil {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final RabbitMqConfigProperties configProperties;

    /**
     * 消息构建器
     */
    public static class MessageBuilder {
        private final Object message;
        private String routingKey;
        private String exchange;
        private MessageProperties messageProperties = new MessageProperties();
        private Map<String, Object> headers = new HashMap<>();

        public MessageBuilder(Object message) {
            this.message = message;
            // 默认设置
            this.messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            this.messageProperties.setContentEncoding("UTF-8");
        }

        public MessageBuilder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public MessageBuilder exchange(String exchange) {
            this.exchange = exchange;
            return this;
        }

        public MessageBuilder ttl(long ttl) {
            this.messageProperties.setExpiration(String.valueOf(ttl));
            return this;
        }

        public MessageBuilder priority(int priority) {
            this.messageProperties.setPriority(priority);
            return this;
        }

        public MessageBuilder persistent(boolean persistent) {
            this.messageProperties.setDeliveryMode(persistent ?
                    MessageDeliveryMode.PERSISTENT : MessageDeliveryMode.NON_PERSISTENT);
            return this;
        }

        public MessageBuilder header(String key, Object value) {
            this.headers.put(key, value);
            return this;
        }

        public MessageBuilder messageId(String messageId) {
            this.messageProperties.setMessageId(messageId);
            return this;
        }

        public MessageBuilder timestamp(LocalDateTime timestamp) {
            this.messageProperties.setTimestamp(java.sql.Timestamp.valueOf(timestamp));
            return this;
        }

        public MessageBuilder userId(String userId) {
            this.messageProperties.setUserId(userId);
            return this;
        }

        public Message build() {
            if (StrUtil.isNotBlank(routingKey)) {
                messageProperties.setReceivedRoutingKey(routingKey);
            }

            // 设置自定义头信息
            headers.forEach(messageProperties::setHeader);

            // 生成消息ID
            if (StrUtil.isBlank(messageProperties.getMessageId())) {
                messageProperties.setMessageId(UUID.randomUUID().toString());
            }

            return new Message(convertToJsonBytes(message), messageProperties);
        }

        private byte[] convertToJsonBytes(Object obj) {
            try {
                if (obj instanceof String) {
                    return ((String) obj).getBytes("UTF-8");
                }
                if (obj instanceof byte[]) {
                    return (byte[]) obj;
                }
                // 这里应该使用注入的MessageConverter，简化示例直接实现
                return cn.hutool.json.JSONUtil.toJsonStr(obj).getBytes("UTF-8");
            } catch (Exception e) {
                throw new RuntimeException("消息序列化失败", e);
            }
        }
    }

    /**
     * 发送消息（同步）
     *
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void send(String routingKey, Object message) {
        send(null, routingKey, message);
    }

    /**
     * 发送消息（同步）
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("发送消息到交换机: {}, 路由键: {}, 消息类型: {}",
                        exchange, routingKey, message.getClass().getSimpleName());
            }

            rabbitTemplate.convertAndSend(exchange, routingKey, message);

            log.info("消息发送成功 - 交换机: {}, 路由键: {}", exchange, routingKey);

        } catch (Exception e) {
            log.error("消息发送失败 - 交换机: {}, 路由键: {}, 错误: {}",
                    exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送消息（异步）
     *
     * @param routingKey 路由键
     * @param message    消息内容
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String routingKey, Object message) {
        return sendAsync(null, routingKey, message);
    }

    /**
     * 发送消息（异步）
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String exchange, String routingKey, Object message) {
        return CompletableFuture.runAsync(() -> send(exchange, routingKey, message))
                .exceptionally(throwable -> {
                    log.error("异步发送消息失败 - 交换机: {}, 路由键: {}, 错误: {}",
                            exchange, routingKey, throwable.getMessage(), throwable);
                    throw new RuntimeException("异步发送消息失败", throwable);
                });
    }

    /**
     * 发送延迟消息
     *
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayTime  延迟时间（毫秒）
     */
    public void sendDelayedMessage(String routingKey, Object message, long delayTime) {
        sendDelayedMessage(null, routingKey, message, delayTime);
    }

    /**
     * 发送延迟消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayTime  延迟时间（毫秒）
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object message, long delayTime) {
        try {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration(String.valueOf(delayTime));
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            Message msg = new Message(cn.hutool.json.JSONUtil.toJsonStr(message).getBytes(), messageProperties);
            rabbitTemplate.send(exchange, routingKey, msg);

            log.info("延迟消息发送成功 - 交换机: {}, 路由键: {}, 延迟时间: {}ms",
                    exchange, routingKey, delayTime);

        } catch (Exception e) {
            log.error("延迟消息发送失败 - 交换机: {}, 路由键: {}, 延迟时间: {}ms, 错误: {}",
                    exchange, routingKey, delayTime, e.getMessage(), e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    /**
     * 发送优先级消息
     *
     * @param routingKey 路由键
     * @param message    消息内容
     * @param priority   优先级（0-255，数值越大优先级越高）
     */
    public void sendPriorityMessage(String routingKey, Object message, int priority) {
        sendPriorityMessage(null, routingKey, message, priority);
    }

    /**
     * 发送优先级消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param priority   优先级（0-255，数值越大优先级越高）
     */
    public void sendPriorityMessage(String exchange, String routingKey, Object message, int priority) {
        try {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setPriority(priority);
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            Message msg = new Message(cn.hutool.json.JSONUtil.toJsonStr(message).getBytes(), messageProperties);
            rabbitTemplate.send(exchange, routingKey, msg);

            log.info("优先级消息发送成功 - 交换机: {}, 路由键: {}, 优先级: {}",
                    exchange, routingKey, priority);

        } catch (Exception e) {
            log.error("优先级消息发送失败 - 交换机: {}, 路由键: {}, 优先级: {}, 错误: {}",
                    exchange, routingKey, priority, e.getMessage(), e);
            throw new RuntimeException("优先级消息发送失败", e);
        }
    }

    /**
     * 发送TTL消息
     *
     * @param routingKey 路由键
     * @param message    消息内容
     * @param ttl        TTL时间（毫秒）
     */
    public void sendTtlMessage(String routingKey, Object message, long ttl) {
        sendTtlMessage(null, routingKey, message, ttl);
    }

    /**
     * 发送TTL消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param ttl        TTL时间（毫秒）
     */
    public void sendTtlMessage(String exchange, String routingKey, Object message, long ttl) {
        try {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration(String.valueOf(ttl));
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            Message msg = new Message(cn.hutool.json.JSONUtil.toJsonStr(message).getBytes(), messageProperties);
            rabbitTemplate.send(exchange, routingKey, msg);

            log.info("TTL消息发送成功 - 交换机: {}, 路由键: {}, TTL: {}ms",
                    exchange, routingKey, ttl);

        } catch (Exception e) {
            log.error("TTL消息发送失败 - 交换机: {}, 路由键: {}, TTL: {}ms, 错误: {}",
                    exchange, routingKey, ttl, e.getMessage(), e);
            throw new RuntimeException("TTL消息发送失败", e);
        }
    }

    /**
     * 批量发送消息
     *
     * @param routingKey 路由键
     * @param messages   消息列表
     */
    public void sendBatch(String routingKey, Object... messages) {
        sendBatch(null, routingKey, messages);
    }

    /**
     * 批量发送消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param messages   消息列表
     */
    public void sendBatch(String exchange, String routingKey, Object... messages) {
        if (messages == null || messages.length == 0) {
            log.warn("批量发送消息失败：消息列表为空");
            return;
        }

        try {
            for (Object message : messages) {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            }

            log.info("批量消息发送成功 - 交换机: {}, 路由键: {}, 消息数量: {}",
                    exchange, routingKey, messages.length);

        } catch (Exception e) {
            log.error("批量消息发送失败 - 交换机: {}, 路由键: {}, 消息数量: {}, 错误: {}",
                    exchange, routingKey, messages.length, e.getMessage(), e);
            throw new RuntimeException("批量消息发送失败", e);
        }
    }

    /**
     * 创建消息构建器
     *
     * @param message 消息内容
     * @return MessageBuilder
     */
    public MessageBuilder messageBuilder(Object message) {
        return new MessageBuilder(message);
    }

    /**
     * 发送自定义消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    自定义消息
     */
    public void sendCustomMessage(String exchange, String routingKey, Message message) {
        try {
            rabbitTemplate.send(exchange, routingKey, message);
            log.info("自定义消息发送成功 - 交换机: {}, 路由键: {}", exchange, routingKey);
        } catch (Exception e) {
            log.error("自定义消息发送失败 - 交换机: {}, 路由键: {}, 错误: {}",
                    exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("自定义消息发送失败", e);
        }
    }

    /**
     * 创建并声明队列
     *
     * @param queueConfig 队列配置
     * @return 队列实例
     */
    public Queue declareQueue(RabbitMqConfigProperties.QueueConfig queueConfig) {
        if (queueConfig == null || StrUtil.isBlank(queueConfig.getName())) {
            throw new IllegalArgumentException("队列配置不能为空");
        }

        Queue queue = QueueBuilder.durable(queueConfig.getName())
                .exclusive(queueConfig.getExclusive())
                .autoDelete(queueConfig.getAutoDelete())
                .withArguments(queueConfig.getArguments() != null ? queueConfig.getArguments() : new HashMap<>())
                .build();

        amqpAdmin.declareQueue(queue);
        log.info("队列创建成功: {}", queueConfig.getName());
        return queue;
    }

    /**
     * 创建并声明交换机
     *
     * @param exchangeConfig 交换机配置
     * @return 交换机实例
     */
    public Exchange declareExchange(RabbitMqConfigProperties.ExchangeConfig exchangeConfig) {
        if (exchangeConfig == null || StrUtil.isBlank(exchangeConfig.getName())) {
            throw new IllegalArgumentException("交换机配置不能为空");
        }

        Exchange exchange;
        switch (exchangeConfig.getType().toLowerCase()) {
            case "direct":
                exchange = ExchangeBuilder.directExchange(exchangeConfig.getName())
                        .durable(exchangeConfig.getDurable())
                        .autoDelete(exchangeConfig.getAutoDelete())
                        .withArguments(exchangeConfig.getArguments() != null ? exchangeConfig.getArguments() : new HashMap<>())
                        .build();
                break;
            case "topic":
                exchange = ExchangeBuilder.topicExchange(exchangeConfig.getName())
                        .durable(exchangeConfig.getDurable())
                        .autoDelete(exchangeConfig.getAutoDelete())
                        .withArguments(exchangeConfig.getArguments() != null ? exchangeConfig.getArguments() : new HashMap<>())
                        .build();
                break;
            case "fanout":
                exchange = ExchangeBuilder.fanoutExchange(exchangeConfig.getName())
                        .durable(exchangeConfig.getDurable())
                        .autoDelete(exchangeConfig.getAutoDelete())
                        .withArguments(exchangeConfig.getArguments() != null ? exchangeConfig.getArguments() : new HashMap<>())
                        .build();
                break;
            case "headers":
                exchange = ExchangeBuilder.headersExchange(exchangeConfig.getName())
                        .durable(exchangeConfig.getDurable())
                        .autoDelete(exchangeConfig.getAutoDelete())
                        .withArguments(exchangeConfig.getArguments() != null ? exchangeConfig.getArguments() : new HashMap<>())
                        .build();
                break;
            default:
                throw new IllegalArgumentException("不支持的交换机类型: " + exchangeConfig.getType());
        }

        amqpAdmin.declareExchange(exchange);
        log.info("交换机创建成功: {} ({})", exchangeConfig.getName(), exchangeConfig.getType());
        return exchange;
    }

    /**
     * 绑定队列到交换机
     *
     * @param queueName    队列名称
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     */
    public void bindQueue(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder.bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);

        amqpAdmin.declareBinding(binding);
        log.info("队列绑定成功 - 队列: {}, 交换机: {}, 路由键: {}", queueName, exchangeName, routingKey);
    }

    /**
     * 发送消息并等待响应（RPC模式）
     *
     * @param routingKey 路由键
     * @param message    请求消息
     * @param replyClass 响应类型
     * @param <T>        响应类型泛型
     * @return 响应消息
     */
    public <T> T sendAndReceive(String routingKey, Object message, Class<T> replyClass) {
        return sendAndReceive(null, routingKey, message, replyClass);
    }

    /**
     * 发送消息并等待响应（RPC模式）
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    请求消息
     * @param replyClass 响应类型
     * @param <T>        响应类型泛型
     * @return 响应消息
     */
    public <T> T sendAndReceive(String exchange, String routingKey, Object message, Class<T> replyClass) {
        try {
            Object response = rabbitTemplate.convertSendAndReceive(exchange, routingKey, message);
            if (response != null) {
                return cn.hutool.json.JSONUtil.toBean(cn.hutool.json.JSONUtil.toJsonStr(response), replyClass);
            }
            return null;

        } catch (Exception e) {
            log.error("RPC调用失败 - 交换机: {}, 路由键: {}, 错误: {}", exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("RPC调用失败", e);
        }
    }
}