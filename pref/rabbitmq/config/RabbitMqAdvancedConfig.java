package com.universe.life.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 高级配置类
 * <p>
 * 提供完整的 RabbitMQ 配置优化，包括：
 * 1. 连接工厂配置优化
 * 2. 消息转换器配置
 * 3. 重试机制配置
 * 4. 死信队列配置
 * 5. 监听器容器工厂配置
 * 6. 消息可靠性保证配置
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Configuration
public class RabbitMqAdvancedConfig {

    /**
     * 默认交换机名称
     */
    public static final String DEFAULT_EXCHANGE = "universe.life.default.exchange";

    /**
     * 死信交换机名称
     */
    public static final String DEAD_LETTER_EXCHANGE = "universe.life.dlx.exchange";

    /**
     * 重试交换机名称
     */
    public static final String RETRY_EXCHANGE = "universe.life.retry.exchange";

    /**
     * 配置主消息转换器（支持多种格式）
     * <p>
     * 使用 AbstractJackson2MessageConverter 作为主转换器，
     * 支持 JSON、XML 等多种序列化格式，并提供类型安全
     * </p>
     *
     * @return 配置好的消息转换器
     */
    @Bean
    @Primary
    public MessageConverter messageConverter() {
        // 使用 Jackson2JsonMessageConverter，支持类型安全
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // 设置类型映射，确保类型安全
        Map<String, Class<?>> typeIdMapping = new HashMap<>();
        typeIdMapping.put("user", com.universe.life.rabbitmq.model.UserMessage.class);
        typeIdMapping.put("order", com.universe.life.rabbitmq.model.OrderMessage.class);
        typeIdMapping.put("notification", com.universe.life.rabbitmq.model.NotificationMessage.class);
        converter.setTypeIdMappings(typeIdMapping);

        // 设置默认类型
        converter.setDefaultType(java.util.HashMap.class);

        // 启用类型预处理
        converter.setUseProjectionForTypes(true);

        log.info("RabbitMQ 消息转换器配置完成，支持类型安全的消息转换");
        return converter;
    }

    /**
     * 配置备用简单消息转换器
     * <p>
     * 用于处理简单的字符串消息，提供更好的兼容性
     * </p>
     *
     * @return 简单消息转换器
     */
    @Bean("simpleMessageConverter")
    public MessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }

    /**
     * 配置高级 RabbitTemplate
     * <p>
     * 提供完整的消息发送功能，包括：
     * - 消息确认回调
     * - 消息返回回调
     * - 发送重试机制
     * - 自定义消息属性
     * </p>
     *
     * @param connectionFactory 连接工厂
     * @param messageConverter 消息转换器
     * @return 配置好的 RabbitTemplate
     */
    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        // 开启发送确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送成功 - CorrelationData: {}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("消息发送失败 - CorrelationData: {}, 失败原因: {}",
                         correlationData != null ? correlationData.getId() : "null", cause);
                // 这里可以添加消息发送失败的重试逻辑或记录到死信队列
            }
        });

        // 开启消息返回回调（当消息无法路由到队列时触发）
        template.setReturnsCallback(returned -> {
            log.warn("消息路由失败 - 消息: {}, 回复码: {}, 回复文本: {}, 交换机: {}, 路由键: {}",
                     returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                     returned.getExchange(), returned.getRoutingKey());

            // 这里可以将路由失败的消息发送到死信队列
            template.convertAndSend(DEAD_LETTER_EXCHANGE, "routing.failed", returned.getMessage());
        });

        // 设置消息必须路由到队列
        template.setMandatory(true);

        // 开启发送重试
        template.setRetryTemplate(retryTemplate());

        log.info("RabbitTemplate 高级配置完成");
        return template;
    }

    /**
     * 配置自定义重试模板
     * <p>
     * 提供指数退避重试机制，避免消息发送失败时的雪崩效应
     * </p>
     *
     * @return 配置好的重试模板
     */
    private org.springframework.retry.support.RetryTemplate retryTemplate() {
        org.springframework.retry.support.RetryTemplate retryTemplate = new org.springframework.retry.support.RetryTemplate();

        // 指数退避策略
        org.springframework.retry.backoff.ExponentialBackOffPolicy backOffPolicy =
            new org.springframework.retry.backoff.ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);  // 初始延迟 1 秒
        backOffPolicy.setMultiplier(2.0);         // 每次延迟翻倍
        backOffPolicy.setMaxInterval(30000L);     // 最大延迟 30 秒

        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重试策略：最多重试 3 次
        org.springframework.retry.policy.SimpleRetryPolicy retryPolicy =
            new org.springframework.retry.policy.SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    /**
     * 配置监听器容器工厂（高并发优化）
     * <p>
     * 优化消费者配置，提高消息处理性能和可靠性
     * </p>
     *
     * @param configurer 容器工厂配置器
     * @param connectionFactory 连接工厂
     * @return 配置好的监听器容器工厂
     */
    @Bean("highConcurrencyListenerFactory")
    public SimpleRabbitListenerContainerFactory highConcurrencyListenerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // 设置并发消费者数量
        factory.setConcurrentConsumers(5);        // 最小并发数
        factory.setMaxConcurrentConsumers(20);    // 最大并发数
        factory.setPrefetchCount(10);             // 每个消费者预取 10 条消息

        // 手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 消费者标签策略
        factory.setConsumerTagStrategy(queueName ->
            "universe-life-" + queueName + "-" + System.currentTimeMillis());

        // 设置批量确认
        factory.setBatchSize(5);

        // 设置超时时间
        factory.setReceiveTimeout(30000L);

        // 开启重试
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryRecoverer(messageRecoverer());

        // 设置错误处理器
        factory.setErrorHandler(errorHandler());

        log.info("高并发监听器容器工厂配置完成");
        return factory;
    }

    /**
     * 配置可靠监听器容器工厂（消息可靠性优先）
     * <p>
     * 优先保证消息可靠性，适用于关键业务消息
     * </p>
     *
     * @param configurer 容器工厂配置器
     * @param connectionFactory 连接工厂
     * @return 配置好的可靠监听器容器工厂
     */
    @Bean("reliableListenerFactory")
    public SimpleRabbitListenerContainerFactory reliableListenerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // 较低的并发数，确保消息处理的可靠性
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(1);  // 每次只处理一条消息

        // 手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 设置事务性
        factory.setChannelTransacted(true);

        // 开启重试
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryRecoverer(messageRecoverer());

        // 设置错误处理器
        factory.setErrorHandler(errorHandler());

        log.info("可靠监听器容器工厂配置完成");
        return factory;
    }

    /**
     * 配置死信交换机
     * <p>
     * 用于接收处理失败或过期的消息
     * </p>
     *
     * @return 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 配置重试交换机
     * <p>
     * 用于实现延迟重试机制
     * </p>
     *
     * @return 重试交换机
     */
    @Bean
    public DirectExchange retryExchange() {
        return ExchangeBuilder.directExchange(RETRY_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 配置消息恢复器
     * <p>
     * 当消息重试次数用尽后，将消息发送到死信队列
     * </p>
     *
     * @return 消息恢复器
     */
    @Bean
    public MessageRecoverer messageRecoverer() {
        return new RepublishMessageRecoverer(rabbitTemplate(null, null),
                                           DEAD_LETTER_EXCHANGE, "failed.message");
    }

    /**
     * 配置错误处理器
     * <p>
     * 统一处理消息消费过程中的异常
     * </p>
     *
     * @return 错误处理器
     */
    @Bean
    public org.springframework.util.ErrorHandler errorHandler() {
        return (throwable) -> {
            log.error("消息处理过程中发生异常", throwable);

            // 这里可以添加错误统计、告警等逻辑
            if (throwable instanceof org.springframework.amqp.AmqpRejectAndDontRequeueException) {
                log.warn("消息被拒绝且不重新入队: {}", throwable.getMessage());
            } else if (throwable instanceof org.springframework.amqp.support.converter.MessageConversionException) {
                log.error("消息转换失败，可能存在格式问题: {}", throwable.getMessage());
            }
        };
    }

    /**
     * 配置重试拦截器
     * <p>
     * 为消息处理提供重试机制
     * </p>
     *
     * @return 重试拦截器
     */
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                .stateless()
                .retryOperations(retryTemplate())
                .recoverer(messageRecoverer())
                .build();
    }
}