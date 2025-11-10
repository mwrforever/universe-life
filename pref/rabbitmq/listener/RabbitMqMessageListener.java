package com.universe.life.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import com.universe.life.rabbitmq.callback.RabbitMqAckCallback;
import com.universe.life.rabbitmq.model.RabbitMqBaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ 消息监听器
 * <p>
 * 提供完整的消息监听处理，包括：
 * 1. 消息接收和解析
 * 2. 手动确认机制
 * 3. 消息重试处理
 * 4. 异常处理和恢复
 * 5. 消息去重
 * 6. 消息处理监控
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Component
public class RabbitMqMessageListener {

    @Autowired
    private RabbitMqAckCallback ackCallback;

    /**
     * 用户消息监听器
     * <p>
     * 处理用户相关的业务消息，支持手动确认和重试机制
     * </p>
     *
     * @param message     消息对象
     * @param channel     通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(
            queues = "universe.life.user.queue",
            containerFactory = "reliableListenerFactory",
            ackMode = "MANUAL"
    )
    public void handleUserMessage(@Payload RabbitMqBaseMessage message,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageId = message.getMessageId();
        log.info("接收到用户消息 - 消息ID: {}, 消息类型: {}", messageId, message.getMessageType());

        try {
            // 检查消息是否重复
            if (isDuplicateMessage(messageId)) {
                log.warn("发现重复消息 - 消息ID: {}, 直接确认", messageId);
                ackCallback.manualAck(channel, deliveryTag, false);
                return;
            }

            // 处理用户消息
            processUserMessage(message);

            // 记录消息已处理
            markMessageAsProcessed(messageId);

            // 手动确认消息
            ackCallback.manualAck(channel, deliveryTag, false);
            log.info("用户消息处理完成 - 消息ID: {}", messageId);

        } catch (BusinessProcessingException e) {
            // 业务处理异常，根据重试策略决定是否重试
            handleBusinessException(message, channel, deliveryTag, e);
        } catch (Exception e) {
            // 系统异常，记录并发送到死信队列
            log.error("处理用户消息时发生系统异常 - 消息ID: {}", messageId, e);
            sendToDeadLetterQueue(message, channel, deliveryTag, "SYSTEM_EXCEPTION");
        }
    }

    /**
     * 订单消息监听器
     * <p>
     * 处理订单相关的业务消息，使用高并发配置
     * </p>
     *
     * @param message     消息对象
     * @param channel     通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(
            queues = "universe.life.order.queue",
            containerFactory = "highConcurrencyListenerFactory",
            ackMode = "MANUAL"
    )
    public void handleOrderMessage(@Payload RabbitMqBaseMessage message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageId = message.getMessageId();
        log.info("接收到订单消息 - 消息ID: {}, 消息类型: {}", messageId, message.getMessageType());

        try {
            // 检查消息是否重复
            if (isDuplicateMessage(messageId)) {
                log.warn("发现重复消息 - 消息ID: {}, 直接确认", messageId);
                ackCallback.manualAck(channel, deliveryTag, false);
                return;
            }

            // 处理订单消息
            processOrderMessage(message);

            // 记录消息已处理
            markMessageAsProcessed(messageId);

            // 手动确认消息
            ackCallback.manualAck(channel, deliveryTag, false);
            log.info("订单消息处理完成 - 消息ID: {}", messageId);

        } catch (BusinessProcessingException e) {
            handleBusinessException(message, channel, deliveryTag, e);
        } catch (Exception e) {
            log.error("处理订单消息时发生系统异常 - 消息ID: {}", messageId, e);
            sendToDeadLetterQueue(message, channel, deliveryTag, "SYSTEM_EXCEPTION");
        }
    }

    /**
     * 通知消息监听器
     * <p>
     * 处理通知相关的消息，支持延迟发送和优先级处理
     * </p>
     *
     * @param message     消息对象
     * @param channel     通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(
            queues = "universe.life.notification.queue",
            containerFactory = "reliableListenerFactory",
            ackMode = "MANUAL"
    )
    public void handleNotificationMessage(@Payload RabbitMqBaseMessage message,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageId = message.getMessageId();
        log.info("接收到通知消息 - 消息ID: {}, 消息类型: {}", messageId, message.getMessageType());

        try {
            // 检查消息是否重复
            if (isDuplicateMessage(messageId)) {
                log.warn("发现重复消息 - 消息ID: {}, 直接确认", messageId);
                ackCallback.manualAck(channel, deliveryTag, false);
                return;
            }

            // 处理通知消息
            processNotificationMessage(message);

            // 记录消息已处理
            markMessageAsProcessed(messageId);

            // 手动确认消息
            ackCallback.manualAck(channel, deliveryTag, false);
            log.info("通知消息处理完成 - 消息ID: {}", messageId);

        } catch (BusinessProcessingException e) {
            handleBusinessException(message, channel, deliveryTag, e);
        } catch (Exception e) {
            log.error("处理通知消息时发生系统异常 - 消息ID: {}", messageId, e);
            sendToDeadLetterQueue(message, channel, deliveryTag, "SYSTEM_EXCEPTION");
        }
    }

    /**
     * 死信队列监听器
     * <p>
     * 监听死信队列，处理失败的消息
     * </p>
     *
     * @param message     原始消息
     * @param channel     通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(
            queues = "universe.life.dlx.queue",
            containerFactory = "reliableListenerFactory",
            ackMode = "MANUAL"
    )
    public void handleDeadLetterMessage(Message message,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageId = message.getMessageProperties().getMessageId();
        log.warn("接收到死信消息 - 消息ID: {}", messageId);

        try {
            // 获取失败信息
            String originalExchange = (String) message.getMessageProperties().getHeader("x-original-exchange");
            String originalRoutingKey = (String) message.getMessageProperties().getHeader("x-original-routing-key");
            String failureReason = (String) message.getMessageProperties().getHeader("x-failure-reason");
            Integer failureCode = (Integer) message.getMessageProperties().getHeader("x-failure-code");

            log.warn("死信消息详情 - 原交换机: {}, 原路由键: {}, 失败原因: {}, 失败码: {}",
                    originalExchange, originalRoutingKey, failureReason, failureCode);

            // 处理死信消息（如记录到数据库、发送告警等）
            processDeadLetterMessage(messageId, originalExchange, originalRoutingKey, failureReason);

            // 确认死信消息
            ackCallback.manualAck(channel, deliveryTag, false);

        } catch (Exception e) {
            log.error("处理死信消息时发生异常 - 消息ID: {}", messageId, e);
            // 死信消息处理失败，仍然确认，避免堆积
            ackCallback.manualAck(channel, deliveryTag, false);
        }
    }

    /**
     * 处理用户消息
     */
    private void processUserMessage(RabbitMqBaseMessage message) throws BusinessProcessingException {
        try {
            // 模拟处理用户消息
            log.info("开始处理用户消息 - 消息ID: {}, 消息内容: {}", message.getMessageId(), message.getPayload());

            // 根据消息类型处理不同的业务逻辑
            switch (message.getMessageType()) {
                case "USER_REGISTER":
                    handleUserRegister(message);
                    break;
                case "USER_UPDATE":
                    handleUserUpdate(message);
                    break;
                case "USER_STATUS_CHANGE":
                    handleUserStatusChange(message);
                    break;
                default:
                    log.warn("未知的用户消息类型 - 消息ID: {}, 类型: {}", message.getMessageId(), message.getMessageType());
            }

            // 模拟处理时间
            TimeUnit.MILLISECONDS.sleep(100);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessProcessingException("用户消息处理被中断", e);
        } catch (Exception e) {
            throw new BusinessProcessingException("用户消息处理失败", e);
        }
    }

    /**
     * 处理订单消息
     */
    private void processOrderMessage(RabbitMqBaseMessage message) throws BusinessProcessingException {
        try {
            log.info("开始处理订单消息 - 消息ID: {}, 消息内容: {}", message.getMessageId(), message.getPayload());

            // 根据消息类型处理不同的订单逻辑
            switch (message.getMessageType()) {
                case "ORDER_CREATE":
                    handleOrderCreate(message);
                    break;
                case "ORDER_PAYMENT_SUCCESS":
                    handleOrderPaymentSuccess(message);
                    break;
                case "ORDER_SHIP":
                    handleOrderShip(message);
                    break;
                case "ORDER_COMPLETE":
                    handleOrderComplete(message);
                    break;
                default:
                    log.warn("未知的订单消息类型 - 消息ID: {}, 类型: {}", message.getMessageId(), message.getMessageType());
            }

            // 模拟处理时间
            TimeUnit.MILLISECONDS.sleep(50);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessProcessingException("订单消息处理被中断", e);
        } catch (Exception e) {
            throw new BusinessProcessingException("订单消息处理失败", e);
        }
    }

    /**
     * 处理通知消息
     */
    private void processNotificationMessage(RabbitMqBaseMessage message) throws BusinessProcessingException {
        try {
            log.info("开始处理通知消息 - 消息ID: {}, 消息内容: {}", message.getMessageId(), message.getPayload());

            // 根据通知类型处理不同的通知逻辑
            String notificationType = (String) message.getExtension("notificationType");
            if (notificationType != null) {
                switch (notificationType) {
                    case "EMAIL":
                        handleEmailNotification(message);
                        break;
                    case "SMS":
                        handleSmsNotification(message);
                        break;
                    case "PUSH":
                        handlePushNotification(message);
                        break;
                    default:
                        log.warn("未知的通知类型 - 消息ID: {}, 类型: {}", message.getMessageId(), notificationType);
                }
            }

            // 模拟处理时间
            TimeUnit.MILLISECONDS.sleep(200);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessProcessingException("通知消息处理被中断", e);
        } catch (Exception e) {
            throw new BusinessProcessingException("通知消息处理失败", e);
        }
    }

    /**
     * 处理业务异常
     */
    private void handleBusinessException(RabbitMqBaseMessage message, Channel channel,
                                       long deliveryTag, BusinessProcessingException e) {
        String messageId = message.getMessageId();

        // 检查是否还能重试
        if (message.canRetry()) {
            message.incrementRetryCount();
            log.warn("业务处理异常，准备重试 - 消息ID: {}, 重试次数: {}/{}, 异常: {}",
                    messageId, message.getRetryCount(), message.getMaxRetryCount(), e.getMessage());

            try {
                // 重新入队进行重试
                ackCallback.manualNackWithRequeue(channel, deliveryTag, false);
            } catch (IOException ioException) {
                log.error("重新入队失败 - 消息ID: {}", messageId, ioException);
                // 如果重新入队失败，发送到死信队列
                sendToDeadLetterQueue(message, channel, deliveryTag, "REQUEUE_FAILED");
            }
        } else {
            log.error("业务处理异常，重试次数已用尽，发送到死信队列 - 消息ID: {}, 异常: {}", messageId, e.getMessage());
            sendToDeadLetterQueue(message, channel, deliveryTag, "RETRY_EXHAUSTED");
        }
    }

    /**
     * 发送消息到死信队列
     */
    private void sendToDeadLetterQueue(RabbitMqBaseMessage message, Channel channel,
                                      long deliveryTag, String reason) {
        try {
            // 添加失败信息到消息扩展属性
            message.addExtension("failureReason", reason);
            message.addExtension("failureTime", System.currentTimeMillis());

            // 拒绝消息且不重新入队
            ackCallback.manualNack(channel, deliveryTag, false);

            log.warn("消息已发送到死信队列 - 消息ID: {}, 原因: {}", message.getMessageId(), reason);
        } catch (IOException e) {
            log.error("发送到死信队列失败 - 消息ID: {}", message.getMessageId(), e);
        }
    }

    /**
     * 检查消息是否重复
     */
    private boolean isDuplicateMessage(String messageId) {
        // 这里可以使用 Redis 等缓存来检查消息是否重复
        // 简化示例，实际应该有去重逻辑
        return false;
    }

    /**
     * 标记消息已处理
     */
    private void markMessageAsProcessed(String messageId) {
        // 这里可以使用 Redis 等缓存来记录已处理的消息
        log.debug("标记消息已处理 - 消息ID: {}", messageId);
    }

    /**
     * 处理死信消息
     */
    private void processDeadLetterMessage(String messageId, String originalExchange,
                                       String originalRoutingKey, String failureReason) {
        log.warn("处理死信消息 - 消息ID: {}, 原交换机: {}, 原路由键: {}, 失败原因: {}",
                messageId, originalExchange, originalRoutingKey, failureReason);

        // 这里可以添加具体的死信消息处理逻辑，如：
        // 1. 记录到数据库
        // 2. 发送告警通知
        // 3. 人工处理工单等
    }

    // 以下是各种具体的业务处理方法
    private void handleUserRegister(RabbitMqBaseMessage message) {
        log.info("处理用户注册消息 - 消息ID: {}", message.getMessageId());
        // 具体的用户注册逻辑
    }

    private void handleUserUpdate(RabbitMqBaseMessage message) {
        log.info("处理用户更新消息 - 消息ID: {}", message.getMessageId());
        // 具体的用户更新逻辑
    }

    private void handleUserStatusChange(RabbitMqBaseMessage message) {
        log.info("处理用户状态变更消息 - 消息ID: {}", message.getMessageId());
        // 具体的用户状态变更逻辑
    }

    private void handleOrderCreate(RabbitMqBaseMessage message) {
        log.info("处理订单创建消息 - 消息ID: {}", message.getMessageId());
        // 具体的订单创建逻辑
    }

    private void handleOrderPaymentSuccess(RabbitMqBaseMessage message) {
        log.info("处理订单支付成功消息 - 消息ID: {}", message.getMessageId());
        // 具体的订单支付成功逻辑
    }

    private void handleOrderShip(RabbitMqBaseMessage message) {
        log.info("处理订单发货消息 - 消息ID: {}", message.getMessageId());
        // 具体的订单发货逻辑
    }

    private void handleOrderComplete(RabbitMqBaseMessage message) {
        log.info("处理订单完成消息 - 消息ID: {}", message.getMessageId());
        // 具体的订单完成逻辑
    }

    private void handleEmailNotification(RabbitMqBaseMessage message) {
        log.info("处理邮件通知消息 - 消息ID: {}", message.getMessageId());
        // 具体的邮件发送逻辑
    }

    private void handleSmsNotification(RabbitMqBaseMessage message) {
        log.info("处理短信通知消息 - 消息ID: {}", message.getMessageId());
        // 具体的短信发送逻辑
    }

    private void handlePushNotification(RabbitMqBaseMessage message) {
        log.info("处理推送通知消息 - 消息ID: {}", message.getMessageId());
        // 具体的推送通知逻辑
    }

    /**
     * 业务处理异常类
     */
    public static class BusinessProcessingException extends Exception {
        public BusinessProcessingException(String message) {
            super(message);
        }

        public BusinessProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}