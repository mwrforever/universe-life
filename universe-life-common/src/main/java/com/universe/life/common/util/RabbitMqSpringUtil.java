package com.universe.life.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

/**
 * RabbitMQ Spring集成工具类和示例
 * <p>
 * 提供完整的RabbitMQ与Spring框架集成示例，包括：
 * <ul>
 *   <li>监听器配置和使用示例</li>
 *   <li>自定义配置类示例</li>
 *   <li>各种消息处理模式示例</li>
 *   <li>错误处理和重试配置示例</li>
 *   <li>生产环境最佳实践</li>
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
public class RabbitMqSpringUtil {

    private final RabbitMqUtil rabbitMqUtil;
    private final RabbitMqAdvancedUtil advancedUtil;
    private final RabbitMqErrorHandler errorHandler;

    /**
     * 基础使用示例
     */
    public void basicUsageExample() {
        // 1. 基础消息发送
        rabbitMqUtil.send("test.queue", "Hello World!");

        // 2. 指定交换机和路由键
        rabbitMqUtil.send("test.exchange", "test.routing.key", "Hello World!");

        // 3. 发送对象消息
        UserMessage userMessage = new UserMessage("张三", 25);
        rabbitMqUtil.send("user.queue", userMessage);

        // 4. 异步发送
        rabbitMqUtil.sendAsync("async.queue", "Async Message");
    }

    /**
     * 高级功能使用示例
     */
    public void advancedUsageExample() {
        // 1. 发送延迟消息
        rabbitMqUtil.sendDelayedMessage("delay.queue", "Delayed Message", 5000);

        // 2. 发送优先级消息
        rabbitMqUtil.sendPriorityMessage("priority.queue", "High Priority Message", 10);

        // 3. 发送TTL消息
        rabbitMqUtil.sendTtlMessage("ttl.queue", "TTL Message", 30000);

        // 4. 批量发送
        rabbitMqUtil.sendBatch("batch.queue", "Message 1", "Message 2", "Message 3");

        // 5. 使用消息构建器
        Message message = rabbitMqUtil.messageBuilder("Complex Message")
                .routingKey("complex.routing.key")
                .exchange("complex.exchange")
                .ttl(60000)
                .priority(5)
                .persistent(true)
                .header("custom-header", "custom-value")
                .build();
        rabbitMqUtil.sendCustomMessage("complex.exchange", "complex.routing.key", message);
    }

    /**
     * RPC调用示例
     */
    public String rpcExample() {
        // 发送RPC请求并等待响应
        String request = "Get User Info";
        String response = rabbitMqUtil.sendAndReceive("rpc.exchange", "rpc.request", request, String.class);
        return response;
    }

    /**
     * 队列和交换机管理示例
     */
    public void queueManagementExample() {
        // 1. 创建基础队列
        Queue queue = QueueBuilder.durable("manual.queue").build();

        // 2. 创建优先级队列
        Queue priorityQueue = advancedUtil.createPriorityQueue("priority.manual.queue", 10);

        // 3. 创建死信队列
        Queue deadLetterQueue = advancedUtil.createDeadLetterQueue(
                "main.manual.queue",
                "dead.manual.queue",
                "dead.manual.exchange",
                "dead.manual.routing.key"
        );

        // 4. 创建TTL队列
        Queue ttlQueue = advancedUtil.createTtlQueue("ttl.manual.queue", 60000);
    }

    /**
     * 错误处理示例
     */
    public void errorHandlingExample() {
        // 1. 带重试的消息发送
        errorHandler.sendWithRetry("retry.exchange", "retry.routing.key", "Retry Message");

        // 2. 异步重试发送
        errorHandler.sendAsyncWithRetry("async.retry.exchange", "async.retry.routing.key", "Async Retry Message");

        // 3. 获取统计信息
        var statistics = errorHandler.getStatistics();
        log.info("错误统计: {}", statistics);
    }

    /**
     * 用户消息示例类
     */
    public static class UserMessage {
        private String name;
        private int age;

        public UserMessage() {}

        public UserMessage(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}

/**
 * RabbitMQ配置类示例
 */
@Configuration
@ConditionalOnBean(RabbitTemplate.class)
class RabbitMqConfiguration {

    /**
     * 自定义RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate customRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // 启用发布确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息确认 - CorrelationId: {}", correlationData.getId());
            } else {
                log.error("消息确认失败 - CorrelationId: {}, 原因: {}", correlationData.getId(), cause);
            }
        });

        // 启用消息返回
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            log.warn("消息返回 - 交换机: {}, 路由键: {}, 回复码: {}, 回复文本: {}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText());
        });

        // 设置消息转换器
        template.setMessageConverter(new RabbitMqMessageConverter());

        return template;
    }

    /**
     * 自定义监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 设置并发消费者数
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);

        // 设置预取数量
        factory.setPrefetchCount(1);

        // 启用手动确认
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 设置重试配置
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}

/**
 * 消息监听器示例类
 */
@Component
@ConditionalOnBean(RabbitTemplate.class)
class RabbitMqMessageListeners {

    /**
     * 基础消息监听器
     */
    @RabbitListener(queues = "test.queue")
    public void handleBasicMessage(String message) {
        log.info("接收到基础消息: {}", message);
    }

    /**
     * 对象消息监听器
     */
    @RabbitListener(queues = "user.queue")
    public void handleUserMessage(RabbitMqSpringUtil.UserMessage userMessage) {
        log.info("接收到用户消息: 姓名={}, 年龄={}", userMessage.getName(), userMessage.getAge());
    }

    /**
     * 手动确认消息监听器
     */
    @RabbitListener(queues = "manual.ack.queue")
    public void handleManualAckMessage(@Payload String message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("接收到手动确认消息: {}", message);

            // 处理消息逻辑
            processMessage(message);

            // 手动确认
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("消息处理失败: {}", e.getMessage(), e);
            try {
                // 拒绝消息并重新入队（或根据情况设为false）
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception ex) {
                log.error("拒绝消息失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 带错误处理的监听器
     */
    @RabbitListener(queues = "error.handling.queue")
    public void handleErrorHandlingMessage(String message) {
        try {
            log.info("接收到需要错误处理的消息: {}", message);

            // 模拟处理失败
            if (message.contains("error")) {
                throw new RuntimeException("模拟处理错误");
            }

            // 正常处理逻辑
            processMessage(message);

        } catch (Exception e) {
            log.error("消息处理异常: {}", e.getMessage(), e);
            // 这里可以实现重试逻辑或发送到死信队列
            throw e; // 重新抛出异常，让Spring AMQP处理
        }
    }

    /**
     * RPC服务监听器
     */
    @RabbitListener(queues = "rpc.request.queue")
    public String handleRpcRequest(String request) {
        log.info("接收到RPC请求: {}", request);

        // 处理请求并返回响应
        String response = "RPC Response: " + request.toUpperCase();
        log.info("返回RPC响应: {}", response);

        return response;
    }

    /**
     * 处理消息的具体逻辑
     */
    private void processMessage(String message) {
        // 实现具体的业务逻辑
        log.debug("处理消息: {}", message);
    }
}