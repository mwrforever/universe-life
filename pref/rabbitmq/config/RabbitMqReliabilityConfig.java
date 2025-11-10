package com.universe.life.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 消息可靠性配置
 * <p>
 * 专门负责消息可靠性的配置，包括：
 * 1. 队列持久化配置
 * 2. 死信队列配置
 * 3. 延迟队列配置
 * 4. 消息 TTL 配置
 * 5. 备份交换机配置
 * 6. 消息去重配置
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Configuration
public class RabbitMqReliabilityConfig {

    // ====== 交换机配置 ======

    /**
     * 主交换机（持久化）
     */
    @Bean("mainExchange")
    public TopicExchange mainExchange() {
        return ExchangeBuilder.topicExchange("universe.life.main.exchange")
                .durable(true)
                .withArgument("alternate-exchange", "universe.life.backup.exchange")
                .build();
    }

    /**
     * 备份交换机（用于接收无法路由的消息）
     */
    @Bean("backupExchange")
    public FanoutExchange backupExchange() {
        return ExchangeBuilder.fanoutExchange("universe.life.backup.exchange")
                .durable(true)
                .build();
    }

    /**
     * 用户交换机
     */
    @Bean("userExchange")
    public DirectExchange userExchange() {
        return ExchangeBuilder.directExchange("universe.life.user.exchange")
                .durable(true)
                .withArgument("alternate-exchange", "universe.life.backup.exchange")
                .build();
    }

    /**
     * 订单交换机
     */
    @Bean("orderExchange")
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange("universe.life.order.exchange")
                .durable(true)
                .withArgument("alternate-exchange", "universe.life.backup.exchange")
                .build();
    }

    /**
     * 通知交换机
     */
    @Bean("notificationExchange")
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange("universe.life.notification.exchange")
                .durable(true)
                .withArgument("alternate-exchange", "universe.life.backup.exchange")
                .build();
    }

    // ====== 队列配置 ======

    /**
     * 用户消息队列（持久化）
     */
    @Bean("userQueue")
    public Queue userQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 300000); // 5分钟 TTL
        arguments.put("x-dead-letter-exchange", "universe.life.dlx.exchange");
        arguments.put("x-dead-letter-routing-key", "user.failed");
        arguments.put("x-max-length", 10000); // 队列最大长度
        arguments.put("x-overflow", "reject-publish"); // 超出长度时拒绝发布

        return QueueBuilder.durable("universe.life.user.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 订单消息队列（持久化）
     */
    @Bean("orderQueue")
    public Queue orderQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 600000); // 10分钟 TTL
        arguments.put("x-dead-letter-exchange", "universe.life.dlx.exchange");
        arguments.put("x-dead-letter-routing-key", "order.failed");
        arguments.put("x-max-length", 50000); // 队列最大长度
        arguments.put("x-overflow", "reject-publish");

        return QueueBuilder.durable("universe.life.order.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 通知消息队列（持久化）
     */
    @Bean("notificationQueue")
    public Queue notificationQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 900000); // 15分钟 TTL
        arguments.put("x-dead-letter-exchange", "universe.life.dlx.exchange");
        arguments.put("x-dead-letter-routing-key", "notification.failed");
        arguments.put("x-max-length", 20000);
        arguments.put("x-overflow", "reject-publish");

        return QueueBuilder.durable("universe.life.notification.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 备份队列（接收无法路由的消息）
     */
    @Bean("backupQueue")
    public Queue backupQueue() {
        return QueueBuilder.durable("universe.life.backup.queue")
                .ttl(300000) // 5分钟 TTL
                .maxLength(1000) // 最大1000条
                .build();
    }

    /**
     * 死信队列（主死信队列）
     */
    @Bean("deadLetterQueue")
    public Queue deadLetterQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 86400000); // 24小时 TTL
        arguments.put("x-max-length", 100000); // 最大10万条

        return QueueBuilder.durable("universe.life.dlx.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 重试队列（用于延迟重试）
     */
    @Bean("retryQueue")
    public Queue retryQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 60000); // 1分钟后重试
        arguments.put("x-dead-letter-exchange", "universe.life.main.exchange");
        arguments.put("x-dead-letter-routing-key", "retry.original");

        return QueueBuilder.durable("universe.life.retry.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 消息去重队列（基于消息ID去重）
     */
    @Bean("deduplicationQueue")
    public Queue deduplicationQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 86400000); // 24小时 TTL
        arguments.put("x-max-length", 1000000); // 最大100万条（用于去重）

        return QueueBuilder.durable("universe.life.deduplication.queue")
                .withArguments(arguments)
                .build();
    }

    // ====== 延迟队列配置 ======

    /**
     * 5秒延迟队列
     */
    @Bean("delay5sQueue")
    public Queue delay5sQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 5000);
        arguments.put("x-dead-letter-exchange", "universe.life.main.exchange");
        arguments.put("x-dead-letter-routing-key", "delay.processed");

        return QueueBuilder.durable("universe.life.delay.5s.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 30秒延迟队列
     */
    @Bean("delay30sQueue")
    public Queue delay30sQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 30000);
        arguments.put("x-dead-letter-exchange", "universe.life.main.exchange");
        arguments.put("x-dead-letter-routing-key", "delay.processed");

        return QueueBuilder.durable("universe.life.delay.30s.queue")
                .withArguments(arguments)
                .build();
    }

    /**
     * 5分钟延迟队列
     */
    @Bean("delay5mQueue")
    public Queue delay5mQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 300000);
        arguments.put("x-dead-letter-exchange", "universe.life.main.exchange");
        arguments.put("x-dead-letter-routing-key", "delay.processed");

        return QueueBuilder.durable("universe.life.delay.5m.queue")
                .withArguments(arguments)
                .build();
    }

    // ====== 绑定配置 ======

    /**
     * 绑定用户队列到用户交换机
     */
    @Bean
    public Binding userQueueBinding() {
        return BindingBuilder.bind(userQueue())
                .to(userExchange())
                .with("user.message");
    }

    /**
     * 绑定订单队列到订单交换机
     */
    @Bean
    public Binding orderQueueBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with("order.message");
    }

    /**
     * 绑定通知队列到通知交换机
     */
    @Bean
    public Binding notificationQueueBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.*");
    }

    /**
     * 绑定备份队列到备份交换机
     */
    @Bean
    public Binding backupQueueBinding() {
        return BindingBuilder.bind(backupQueue())
                .to(backupExchange());
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(ExchangeBuilder.directExchange("universe.life.dlx.exchange").build())
                .with("failed.message");
    }

    /**
     * 绑定重试队列到重试交换机
     */
    @Bean
    public Binding retryQueueBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(ExchangeBuilder.directExchange("universe.life.retry.exchange").build())
                .with("retry.message");
    }

    /**
     * 绑定延迟队列到延迟交换机
     */
    @Bean
    public Binding delay5sQueueBinding() {
        return BindingBuilder.bind(delay5sQueue())
                .to(ExchangeBuilder.directExchange("universe.life.delay.exchange").build())
                .with("delay.5s");
    }

    /**
     * 绑定延迟队列到延迟交换机
     */
    @Bean
    public Binding delay30sQueueBinding() {
        return BindingBuilder.bind(delay30sQueue())
                .to(ExchangeBuilder.directExchange("universe.life.delay.exchange").build())
                .with("delay.30s");
    }

    /**
     * 绑定延迟队列到延迟交换机
     */
    @Bean
    public Binding delay5mQueueBinding() {
        return BindingBuilder.bind(delay5mQueue())
                .to(ExchangeBuilder.directExchange("universe.life.delay.exchange").build())
                .with("delay.5m");
    }

    /**
     * RabbitAdmin 管理器
     * 用于自动声明队列、交换机和绑定
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        rabbitAdmin.setIgnoreDeclarationExceptions(true);
        return rabbitAdmin;
    }

    /**
     * 自定义消息恢复器
     * 处理重试失败后的消息
     */
    @Bean("customMessageRecoverer")
    public MessageRecoverer customMessageRecoverer() {
        return new CustomMessageRecoverer();
    }

    /**
     * 自定义重试拦截器
     */
    @Bean("customRetryInterceptor")
    public RetryOperationsInterceptor customRetryInterceptor() {
        return org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                .stateless()
                .retryOperations(retryTemplate())
                .recoverer(customMessageRecoverer())
                .build();
    }

    /**
     * 自定义重试模板
     */
    private org.springframework.retry.support.RetryTemplate retryTemplate() {
        org.springframework.retry.support.RetryTemplate retryTemplate = new org.springframework.retry.support.RetryTemplate();

        // 指数退避策略
        org.springframework.retry.backoff.ExponentialBackOffPolicy backOffPolicy =
                new org.springframework.retry.backoff.ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000L); // 初始延迟 2 秒
        backOffPolicy.setMultiplier(2.0);        // 每次延迟翻倍
        backOffPolicy.setMaxInterval(60000L);    // 最大延迟 60 秒

        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重试策略
        org.springframework.retry.policy.SimpleRetryPolicy retryPolicy =
                new org.springframework.retry.policy.SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5); // 最多重试 5 次

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    /**
     * 自定义消息恢复器实现
     */
    public static class CustomMessageRecoverer implements MessageRecoverer {
        @Override
        public void recover(Message message, Throwable cause) {
            log.error("消息重试次数已用尽，进入恢复流程 - 消息ID: {}, 原因: {}",
                    message.getMessageProperties().getMessageId(), cause.getMessage());

            // 这里可以实现自定义的恢复逻辑，如：
            // 1. 发送告警
            // 2. 记录到数据库
            // 3. 发送到人工处理队列
            // 4. 调用第三方通知服务等
        }
    }
}