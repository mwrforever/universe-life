package com.universe.life.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * RabbitMQ异常处理器和重试机制
 * <p>
 * 提供RabbitMQ消息处理的异常处理、重试机制和死信处理等功能
 * 支持可配置的重试策略、指数退避算法和自定义异常处理逻辑
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>消息发送重试机制</li>
 *   <li>消费异常处理</li>
 *   <li>发送确认和返回处理</li>
 *   <li>指数退避重试策略</li>
 *   <li>自定义异常处理逻辑</li>
 *   <li>死信队列转发</li>
 *   <li>错误统计和监控</li>
 * </ul>
 *
 * @author universe-life
 * @version 1.0.0
 * @since 2024-11-10
 */
@Slf4j
@Component
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitMqErrorHandler {

    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate retryTemplate;
    private final MessageConverter messageConverter;

    // 错误统计
    private final Map<String, Long> errorCountMap = new HashMap<>();
    private final Map<String, Long> retryCountMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param rabbitTemplate RabbitMQ模板
     */
    @Autowired
    public RabbitMqErrorHandler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = rabbitTemplate.getMessageConverter();
        this.retryTemplate = createRetryTemplate();

        // 配置发送确认回调
        configureConfirmCallback();
        configureReturnCallback();
    }

    /**
     * 创建重试模板
     *
     * @return 配置好的重试模板
     */
    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 最大重试次数

        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 初始间隔1秒
        backOffPolicy.setMultiplier(2.0);        // 间隔翻倍
        backOffPolicy.setMaxInterval(10000);     // 最大间隔10秒

        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    /**
     * 配置发送确认回调
     */
    private void configureConfirmCallback() {
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            if (ack) {
                log.info("消息发送确认成功 - CorrelationId: {}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("消息发送确认失败 - CorrelationId: {}, 原因: {}",
                        correlationData != null ? correlationData.getId() : "null", cause);
                handleSendFailure(correlationData, cause);
            }
        });
    }

    /**
     * 配置发送返回回调
     */
    private void configureReturnCallback() {
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("消息返回 - 消息: {}, 回复码: {}, 回复文本: {}, 交换机: {}, 路由键: {}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
            handleMessageReturn(returned);
        });
    }

    /**
     * 带重试的消息发送
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param <T>        消息类型
     */
    public <T> void sendWithRetry(String exchange, String routingKey, T message) {
        try {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                try {
                    rabbitTemplate.convertAndSend(exchange, routingKey, message);
                    return null;
                } catch (Exception e) {
                    log.warn("消息发送失败，准备第 {} 次重试", context.getRetryCount() + 1, e);
                    throw e;
                }
            }, context -> {
                log.error("消息发送重试失败 - 交换机: {}, 路由键: {}, 重试次数: {}",
                        exchange, routingKey, context.getRetryCount(), context.getLastThrowable());
                throw new RuntimeException("消息发送重试失败", context.getLastThrowable());
            });

        } catch (Exception e) {
            log.error("消息发送异常 - 交换机: {}, 路由键: {}, 错误: {}", exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("消息发送异常", e);
        }
    }

    /**
     * 带重试的异步消息发送
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param <T>        消息类型
     * @return CompletableFuture
     */
    public <T> CompletableFuture<Void> sendAsyncWithRetry(String exchange, String routingKey, T message) {
        return CompletableFuture.runAsync(() -> sendWithRetry(exchange, routingKey, message))
                .exceptionally(throwable -> {
                    log.error("异步重试发送消息失败 - 交换机: {}, 路由键: {}, 错误: {}",
                            exchange, routingKey, throwable.getMessage(), throwable);
                    throw new RuntimeException("异步重试发送消息失败", throwable);
                });
    }

    /**
     * 处理消费异常
     *
     * @param message     消息
     * @param exception   异常
     * @param errorHandler 自定义异常处理器
     */
    public void handleConsumerException(Message message, Exception exception, Function<Exception, Boolean> errorHandler) {
        String messageId = message.getMessageProperties().getMessageId();
        String queueName = message.getMessageProperties().getConsumerQueue();

        // 统计错误
        errorCountMap.merge(queueName, 1L, Long::sum);

        log.error("消费异常 - 队列: {}, 消息ID: {}, 错误: {}", queueName, messageId, exception.getMessage(), exception);

        try {
            // 执行自定义异常处理
            boolean shouldRetry = errorHandler != null ? errorHandler.apply(exception) : false;

            if (shouldRetry) {
                handleRetry(message, queueName);
            } else {
                handleDeadLetter(message, queueName, exception);
            }

        } catch (Exception e) {
            log.error("异常处理失败 - 队列: {}, 消息ID: {}, 错误: {}", queueName, messageId, e.getMessage(), e);
            handleDeadLetter(message, queueName, e);
        }
    }

    /**
     * 处理消息重试
     *
     * @param message    消息
     * @param queueName  队列名称
     */
    private void handleRetry(Message message, String queueName) {
        try {
            // 检查重试次数
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount >= 3) {
                log.warn("消息重试次数已达上限 - 队列: {}, 消息ID: {}, 重试次数: {}",
                        queueName, message.getMessageProperties().getMessageId(), retryCount);
                handleDeadLetter(message, queueName, new RuntimeException("重试次数超限"));
                return;
            }

            // 增加重试次数
            message.getMessageProperties().getHeaders().put("x-retry-count", retryCount + 1);

            // 重新发送到原队列（延迟发送）
            Thread.sleep(Math.min(1000 * (retryCount + 1), 5000)); // 指数退避，最大5秒

            rabbitTemplate.send("", queueName, message);
            retryCountMap.merge(queueName, 1L, Long::sum);

            log.info("消息重试发送 - 队列: {}, 消息ID: {}, 重试次数: {}",
                    queueName, message.getMessageProperties().getMessageId(), retryCount + 1);

        } catch (Exception e) {
            log.error("消息重试失败 - 队列: {}, 消息ID: {}, 错误: {}",
                    queueName, message.getMessageProperties().getMessageId(), e.getMessage(), e);
            handleDeadLetter(message, queueName, e);
        }
    }

    /**
     * 处理死信
     *
     * @param message   消息
     * @param queueName 队列名称
     * @param exception 原异常
     */
    private void handleDeadLetter(Message message, String queueName, Exception exception) {
        try {
            // 添加异常信息到消息头
            message.getMessageProperties().getHeaders().put("x-exception-message", exception.getMessage());
            message.getMessageProperties().getHeaders().put("x-exception-class", exception.getClass().getName());
            message.getMessageProperties().getHeaders().put("x-original-queue", queueName);
            message.getMessageProperties().getHeaders().put("x-failure-timestamp", System.currentTimeMillis());

            // 发送到死信队列
            String deadLetterQueue = queueName + ".dead";
            rabbitTemplate.send("", deadLetterQueue, message);

            log.info("消息发送到死信队列 - 原队列: {}, 死信队列: {}, 消息ID: {}",
                    queueName, deadLetterQueue, message.getMessageProperties().getMessageId());

        } catch (Exception e) {
            log.error("死信处理失败 - 队列: {}, 消息ID: {}, 错误: {}",
                    queueName, message.getMessageProperties().getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * 处理发送失败
     *
     * @param correlationData 关联数据
     * @param cause           失败原因
     */
    private void handleSendFailure(CorrelationData correlationData, String cause) {
        try {
            log.error("处理发送失败 - CorrelationId: {}, 原因: {}", correlationData.getId(), cause);

            // 这里可以实现发送失败的处理逻辑，比如：
            // 1. 记录到数据库
            // 2. 发送到重试队列
            // 3. 发送告警通知

        } catch (Exception e) {
            log.error("发送失败处理异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理消息返回
     *
     * @param returned 返回的消息
     */
    private void handleMessageReturn(org.springframework.amqp.core.Returned returned) {
        try {
            log.warn("处理消息返回 - 交换机: {}, 路由键: {}, 回复码: {}, 回复文本: {}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText());

            // 这里可以实现消息返回的处理逻辑，比如：
            // 1. 发送到备份队列
            // 2. 记录返回日志
            // 3. 发送告警

        } catch (Exception e) {
            log.error("消息返回处理异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理监听器执行失败异常
     *
     * @param exception 监听器执行失败异常
     */
    public void handleListenerExecutionFailedException(ListenerExecutionFailedException exception) {
        try {
            Message message = exception.getFailedMessage();
            if (message != null) {
                String messageId = message.getMessageProperties().getMessageId();
                String queueName = message.getMessageProperties().getConsumerQueue();

                log.error("监听器执行失败 - 队列: {}, 消息ID: {}, 错误: {}",
                        queueName, messageId, exception.getMessage(), exception);

                handleConsumerException(message, exception.getCause(), null);
            }

        } catch (Exception e) {
            log.error("监听器异常处理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建自定义重试模板
     *
     * @param maxAttempts 最大重试次数
     * @param initialInterval 初始间隔（毫秒）
     * @param multiplier     间隔倍数
     * @param maxInterval    最大间隔（毫秒）
     * @return 自定义重试模板
     */
    public RetryTemplate createCustomRetryTemplate(int maxAttempts, long initialInterval,
                                                  double multiplier, long maxInterval) {
        RetryTemplate template = new RetryTemplate();

        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);

        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    /**
     * 获取错误统计
     *
     * @param queueName 队列名称
     * @return 错误次数
     */
    public long getErrorCount(String queueName) {
        return errorCountMap.getOrDefault(queueName, 0L);
    }

    /**
     * 获取重试统计
     *
     * @param queueName 队列名称
     * @return 重试次数
     */
    public long getRetryCount(String queueName) {
        return retryCountMap.getOrDefault(queueName, 0L);
    }

    /**
     * 获取所有统计信息
     *
     * @return 统计信息Map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("errorCount", new HashMap<>(errorCountMap));
        statistics.put("retryCount", new HashMap<>(retryCountMap));
        return statistics;
    }

    /**
     * 重置统计信息
     *
     * @param queueName 队列名称，如果为null则重置所有
     */
    public void resetStatistics(String queueName) {
        if (StrUtil.isBlank(queueName)) {
            errorCountMap.clear();
            retryCountMap.clear();
            log.info("所有统计信息已重置");
        } else {
            errorCountMap.remove(queueName);
            retryCountMap.remove(queueName);
            log.info("队列 {} 的统计信息已重置", queueName);
        }
    }
}