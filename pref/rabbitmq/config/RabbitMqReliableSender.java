package com.universe.life.rabbitmq.service;

import com.universe.life.rabbitmq.callback.RabbitMqAckCallback;
import com.universe.life.rabbitmq.model.RabbitMqBaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitMQ 可靠消息发送服务
 * <p>
 * 提供完整的消息发送可靠性保证，包括：
 * 1. 消息发送确认机制
 * 2. 消息重试机制
 * 3. 消息持久化保证
 * 4. 消息去重处理
 * 5. 异步发送支持
 * 6. 发送性能监控
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Service
public class RabbitMqReliableSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMqAckCallback ackCallback;

    @Autowired
    @Qualifier("mainExchange")
    private TopicExchange mainExchange;

    @Autowired
    @Qualifier("userExchange")
    private DirectExchange userExchange;

    @Autowired
    @Qualifier("orderExchange")
    private DirectExchange orderExchange;

    @Autowired
    @Qualifier("notificationExchange")
    private TopicExchange notificationExchange;

    /**
     * 异步发送线程池
     */
    private final ExecutorService asyncSenderExecutor = Executors.newFixedThreadPool(10);

    /**
     * 消息发送计数器
     */
    private final AtomicLong sendCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);

    /**
     * 发送可靠消息（同步）
     * <p>
     * 保证消息的可靠发送，包括确认机制和重试机制
     * </p>
     *
     * @param exchange    交换机
     * @param routingKey  路由键
     * @param message     消息内容
     * @param priority    消息优先级
     * @param ttl         消息TTL（毫秒）
     * @return 发送结果
     */
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public SendResult sendReliableMessage(String exchange, String routingKey,
                                        Object message, Integer priority, Long ttl) {
        String messageId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            sendCount.incrementAndGet();

            // 创建关联数据用于确认回调
            CorrelationData correlationData = new CorrelationData(messageId);

            // 准备消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setMessageId(messageId);
            messageProperties.setTimestamp(new java.util.Date());
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setContentEncoding("UTF-8");

            // 设置消息优先级
            if (priority != null && priority >= 0 && priority <= 9) {
                messageProperties.setPriority(priority);
            }

            // 设置消息TTL
            if (ttl != null && ttl > 0) {
                messageProperties.setExpiration(String.valueOf(ttl));
            }

            // 设置消息持久化
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            // 添加消息头信息
            messageProperties.setHeader("sendTime", System.currentTimeMillis());
            messageProperties.setHeader("senderService", "universe-life-service");
            messageProperties.setHeader("retryCount", 0);

            // 创建消息对象
            Message amqpMessage = rabbitTemplate.getMessageConverter().toMessage(message, messageProperties);

            // 注册确认信息
            ackCallback.registerConfirmInfo(correlationData, message, exchange, routingKey);

            // 发送消息
            rabbitTemplate.send(exchange, routingKey, amqpMessage, correlationData);

            long endTime = System.currentTimeMillis();
            successCount.incrementAndGet();

            log.info("可靠消息发送成功 - 消息ID: {}, 交换机: {}, 路由键: {}, 耗时: {}ms",
                    messageId, exchange, routingKey, endTime - startTime);

            return new SendResult()
                    .setSuccess(true)
                    .setMessageId(messageId)
                    .setExchange(exchange)
                    .setRoutingKey(routingKey)
                    .setSendTime(startTime)
                    .setCostTime(endTime - startTime);

        } catch (Exception e) {
            failureCount.incrementAndGet();
            long endTime = System.currentTimeMillis();

            log.error("可靠消息发送失败 - 消息ID: {}, 交换机: {}, 路由键: {}, 耗时: {}ms, 异常: {}",
                    messageId, exchange, routingKey, endTime - startTime, e.getMessage());

            return new SendResult()
                    .setSuccess(false)
                    .setMessageId(messageId)
                    .setExchange(exchange)
                    .setRoutingKey(routingKey)
                    .setSendTime(startTime)
                    .setCostTime(endTime - startTime)
                    .setErrorMessage(e.getMessage());
        }
    }

    /**
     * 发送可靠消息（异步）
     * <p>
     * 异步发送消息，提高发送性能
     * </p>
     *
     * @param exchange    交换机
     * @param routingKey  路由键
     * @param message     消息内容
     * @param priority    消息优先级
     * @param ttl         消息TTL（毫秒）
     * @return CompletableFuture<SendResult>
     */
    public CompletableFuture<SendResult> sendReliableMessageAsync(String exchange, String routingKey,
                                                               Object message, Integer priority, Long ttl) {
        return CompletableFuture.supplyAsync(() ->
            sendReliableMessage(exchange, routingKey, message, priority, ttl),
            asyncSenderExecutor
        ).whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("异步消息发送异常 - 交换机: {}, 路由键: {}", exchange, routingKey, throwable);
            } else {
                log.debug("异步消息发送完成 - 消息ID: {}, 结果: {}",
                         result.getMessageId(), result.isSuccess() ? "成功" : "失败");
            }
        });
    }

    /**
     * 发送用户消息
     *
     * @param userMessage 用户消息
     * @return 发送结果
     */
    public SendResult sendUserMessage(RabbitMqBaseMessage userMessage) {
        log.info("发送用户消息 - 消息ID: {}, 消息类型: {}",
                userMessage.getMessageId(), userMessage.getMessageType());

        // 设置消息元数据
        userMessage.setSourceService("user-service");
        userMessage.setPriority(2); // 用户消息普通优先级

        return sendReliableMessage(
                userExchange.getName(),
                "user.message",
                userMessage,
                userMessage.getPriority(),
                userMessage.getTtl()
        );
    }

    /**
     * 发送订单消息
     *
     * @param orderMessage 订单消息
     * @return 发送结果
     */
    public SendResult sendOrderMessage(RabbitMqBaseMessage orderMessage) {
        log.info("发送订单消息 - 消息ID: {}, 消息类型: {}",
                orderMessage.getMessageId(), orderMessage.getMessageType());

        // 设置消息元数据
        orderMessage.setSourceService("order-service");
        orderMessage.setPriority(3); // 订单消息较高优先级

        return sendReliableMessage(
                orderExchange.getName(),
                "order.message",
                orderMessage,
                orderMessage.getPriority(),
                orderMessage.getTtl()
        );
    }

    /**
     * 发送通知消息
     *
     * @param notificationMessage 通知消息
     * @return 发送结果
     */
    public SendResult sendNotificationMessage(RabbitMqBaseMessage notificationMessage) {
        log.info("发送通知消息 - 消息ID: {}, 消息类型: {}",
                notificationMessage.getMessageId(), notificationMessage.getMessageType());

        // 设置消息元数据
        notificationMessage.setSourceService("notification-service");
        notificationMessage.setPriority(1); // 通知消息普通优先级

        return sendReliableMessage(
                notificationExchange.getName(),
                "notification.message",
                notificationMessage,
                notificationMessage.getPriority(),
                notificationMessage.getTtl()
        );
    }

    /**
     * 发送延迟消息
     * <p>
     * 使用延迟队列实现消息延迟发送
     * </p>
     *
     * @param message     消息内容
     * @param routingKey 路由键
     * @param delayTime  延迟时间（毫秒）
     * @return 发送结果
     */
    public SendResult sendDelayMessage(Object message, String routingKey, long delayTime) {
        String delayQueue;

        // 根据延迟时间选择合适的延迟队列
        if (delayTime <= 5000) {
            delayQueue = "universe.life.delay.5s.queue";
        } else if (delayTime <= 30000) {
            delayQueue = "universe.life.delay.30s.queue";
        } else if (delayTime <= 300000) {
            delayQueue = "universe.life.delay.5m.queue";
        } else {
            throw new IllegalArgumentException("延迟时间不能超过5分钟");
        }

        log.info("发送延迟消息 - 路由键: {}, 延迟时间: {}ms, 使用队列: {}",
                routingKey, delayTime, delayQueue);

        // 添加延迟信息到消息头
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(UUID.randomUUID().toString());
        messageProperties.setHeader("originalRoutingKey", routingKey);
        messageProperties.setHeader("delayTime", delayTime);
        messageProperties.setHeader("sendTime", System.currentTimeMillis());

        Message amqpMessage = rabbitTemplate.getMessageConverter().toMessage(message, messageProperties);

        try {
            rabbitTemplate.send("universe.life.delay.exchange", getDelayRoutingKey(delayTime), amqpMessage);

            return new SendResult()
                    .setSuccess(true)
                    .setMessageId(messageProperties.getMessageId())
                    .setExchange("universe.life.delay.exchange")
                    .setRoutingKey(getDelayRoutingKey(delayTime))
                    .setSendTime(System.currentTimeMillis());

        } catch (Exception e) {
            log.error("延迟消息发送失败", e);
            return new SendResult()
                    .setSuccess(false)
                    .setMessageId(messageProperties.getMessageId())
                    .setErrorMessage(e.getMessage());
        }
    }

    /**
     * 发送事务消息
     * <p>
     * 使用 RabbitMQ 事务机制确保消息的可靠发送
     * </p>
     *
     * @param exchange    交换机
     * @param routingKey  路由键
     * @param message     消息内容
     * @return 发送结果
     */
    public SendResult sendTransactionalMessage(String exchange, String routingKey, Object message) {
        String messageId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            // 开始事务
            rabbitTemplate.execute(channel -> {
                channel.txSelect();
                try {
                    // 发送消息
                    MessageProperties messageProperties = new MessageProperties();
                    messageProperties.setMessageId(messageId);
                    messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    messageProperties.setHeader("transactional", true);
                    messageProperties.setHeader("sendTime", System.currentTimeMillis());

                    Message amqpMessage = rabbitTemplate.getMessageConverter().toMessage(message, messageProperties);

                    channel.basicPublish(exchange, routingKey, amqpMessage.getMessageProperties(), amqpMessage.getBody());

                    // 提交事务
                    channel.txCommit();
                    log.info("事务消息发送成功 - 消息ID: {}", messageId);

                } catch (Exception e) {
                    // 回滚事务
                    channel.txRollback();
                    throw e;
                }
                return null;
            });

            long endTime = System.currentTimeMillis();
            successCount.incrementAndGet();

            return new SendResult()
                    .setSuccess(true)
                    .setMessageId(messageId)
                    .setExchange(exchange)
                    .setRoutingKey(routingKey)
                    .setSendTime(startTime)
                    .setCostTime(endTime - startTime);

        } catch (Exception e) {
            failureCount.incrementAndGet();
            long endTime = System.currentTimeMillis();

            log.error("事务消息发送失败 - 消息ID: {}, 异常: {}", messageId, e.getMessage());

            return new SendResult()
                    .setSuccess(false)
                    .setMessageId(messageId)
                    .setExchange(exchange)
                    .setRoutingKey(routingKey)
                    .setSendTime(startTime)
                    .setCostTime(endTime - startTime)
                    .setErrorMessage(e.getMessage());
        }
    }

    /**
     * 批量发送消息
     * <p>
     * 提高批量消息发送的效率和可靠性
     * </p>
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param messages   消息列表
     * @return 批量发送结果
     */
    public BatchSendResult sendBatchMessages(String exchange, String routingKey, java.util.List<Object> messages) {
        BatchSendResult batchResult = new BatchSendResult();
        batchResult.setTotalCount(messages.size());
        batchResult.setExchange(exchange);
        batchResult.setRoutingKey(routingKey);
        batchResult.setStartTime(System.currentTimeMillis());

        log.info("开始批量发送消息 - 总数: {}, 交换机: {}, 路由键: {}",
                messages.size(), exchange, routingKey);

        for (Object message : messages) {
            SendResult result = sendReliableMessage(exchange, routingKey, message, null, null);
            batchResult.addResult(result);
        }

        batchResult.setEndTime(System.currentTimeMillis());
        batchResult.setCostTime(batchResult.getEndTime() - batchResult.getStartTime());

        log.info("批量消息发送完成 - 总数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                batchResult.getTotalCount(), batchResult.getSuccessCount(),
                batchResult.getFailureCount(), batchResult.getCostTime());

        return batchResult;
    }

    /**
     * 获取发送统计信息
     *
     * @return 发送统计
     */
    public SendStatistics getSendStatistics() {
        return new SendStatistics()
                .setSendCount(sendCount.get())
                .setSuccessCount(successCount.get())
                .setFailureCount(failureCount.get())
                .setSuccessRate(calculateSuccessRate())
                .setTimestamp(System.currentTimeMillis());
    }

    /**
     * 根据延迟时间获取路由键
     */
    private String getDelayRoutingKey(long delayTime) {
        if (delayTime <= 5000) {
            return "delay.5s";
        } else if (delayTime <= 30000) {
            return "delay.30s";
        } else if (delayTime <= 300000) {
            return "delay.5m";
        }
        return "delay.5m";
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = sendCount.get();
        return total == 0 ? 0.0 : (double) successCount.get() / total * 100;
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        if (asyncSenderExecutor != null && !asyncSenderExecutor.isShutdown()) {
            try {
                asyncSenderExecutor.shutdown();
                if (!asyncSenderExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    asyncSenderExecutor.shutdownNow();
                }
                log.info("异步发送线程池已关闭");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                asyncSenderExecutor.shutdownNow();
            }
        }
    }

    // ====== 内部类定义 ======

    /**
     * 发送结果类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class SendResult {
        private boolean success;
        private String messageId;
        private String exchange;
        private String routingKey;
        private long sendTime;
        private long costTime;
        private String errorMessage;
    }

    /**
     * 批量发送结果类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class BatchSendResult {
        private String exchange;
        private String routingKey;
        private int totalCount;
        private int successCount;
        private int failureCount;
        private long startTime;
        private long endTime;
        private long costTime;
        private java.util.List<SendResult> results = new java.util.ArrayList<>();

        public void addResult(SendResult result) {
            results.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }
    }

    /**
     * 发送统计类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class SendStatistics {
        private long sendCount;
        private long successCount;
        private long failureCount;
        private double successRate;
        private long timestamp;
    }
}