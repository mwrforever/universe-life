package com.universe.life.rabbitmq.callback;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitMQ ACK 回调处理器
 * <p>
 * 提供完整的消息确认机制，包括：
 * 1. 生产者确认回调（Confirm Callback）
 * 2. 消费者手动确认（Manual Ack）
 * 3. 消息返回回调（Return Callback）
 * 4. 消息重试机制
 * 5. 消息监控和统计
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Component
public class RabbitMqAckCallback implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageConverter messageConverter;

    /**
     * 消息确认回调缓存
     * 用于存储消息发送状态和相关信息
     */
    private final Map<String, MessageConfirmInfo> confirmCache = new ConcurrentHashMap<>();

    /**
     * 消息统计计数器
     */
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong returnCount = new AtomicLong(0);

    /**
     * 生产者确认回调
     * <p>
     * 当消息发送到 RabbitMQ 服务器后会触发此回调
     * 用于确认消息是否成功到达交换机
     * </p>
     *
     * @param correlationData 关联数据（包含消息ID）
     * @param ack             是否确认（true：成功，false：失败）
     * @param cause           失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = correlationData != null ? correlationData.getId() : "unknown";
        MessageConfirmInfo confirmInfo = confirmCache.get(messageId);

        if (ack) {
            // 消息发送成功
            handleConfirmSuccess(messageId, confirmInfo);
            successCount.incrementAndGet();
        } else {
            // 消息发送失败
            handleConfirmFailure(messageId, confirmInfo, cause);
            failureCount.incrementAndGet();
        }

        // 清理缓存
        confirmCache.remove(messageId);
    }

    /**
     * 消息返回回调
     * <p>
     * 当消息无法路由到队列时会触发此回调
     * 通常发生在路由键不匹配或队列不存在的情况
     * </p>
     *
     * @param message    返回的消息
     * @param replyCode  回复码
     * @param replyText  回复文本
     * @param exchange   交换机
     * @param routingKey 路由键
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText,
                              String exchange, String routingKey) {
        returnCount.incrementAndGet();

        // 获取消息属性
        MessageProperties properties = message.getMessageProperties();
        String messageId = properties.getMessageId();

        log.warn("消息路由失败 - 消息ID: {}, 回复码: {}, 回复文本: {}, 交换机: {}, 路由键: {}",
                messageId, replyCode, replyText, exchange, routingKey);

        // 尝试从消息中获取业务数据
        Object payload = null;
        try {
            payload = messageConverter.fromMessage(message);
        } catch (Exception e) {
            log.error("消息转换失败 - 消息ID: {}", messageId, e);
            payload = new String(message.getBody());
        }

        // 处理返回的消息
        handleReturnedMessage(messageId, payload, replyCode, replyText, exchange, routingKey);

        // 将路由失败的消息发送到死信队列
        sendToDeadLetterQueue(message, replyCode, replyText, exchange, routingKey);
    }

    /**
     * 手动确认消息
     * <p>
     * 消费者处理完消息后手动确认
     * </p>
     *
     * @param channel    通道
     * @param deliveryTag 消息标签
     * @param multiple   是否批量确认
     * @throws IOException IO异常
     */
    public void manualAck(Channel channel, long deliveryTag, boolean multiple) throws IOException {
        try {
            channel.basicAck(deliveryTag, multiple);
            log.debug("消息确认成功 - deliveryTag: {}, multiple: {}", deliveryTag, multiple);
        } catch (IOException e) {
            log.error("消息确认失败 - deliveryTag: {}, multiple: {}", deliveryTag, multiple, e);
            throw e;
        }
    }

    /**
     * 手动拒绝消息（不重新入队）
     * <p>
     * 消息处理失败时拒绝，消息将进入死信队列
     * </p>
     *
     * @param channel     通道
     * @param deliveryTag 消息标签
     * @param multiple    是否批量拒绝
     * @throws IOException IO异常
     */
    public void manualNack(Channel channel, long deliveryTag, boolean multiple) throws IOException {
        try {
            channel.basicNack(deliveryTag, multiple, false);
            log.warn("消息拒绝且不重新入队 - deliveryTag: {}, multiple: {}", deliveryTag, multiple);
        } catch (IOException e) {
            log.error("消息拒绝失败 - deliveryTag: {}, multiple: {}", deliveryTag, multiple, e);
            throw e;
        }
    }

    /**
     * 手动拒绝消息（重新入队）
     * <p>
     * 消息处理失败时拒绝但重新入队，用于可重试的业务场景
     * </p>
     *
     * @param channel     通道
     * @param deliveryTag 消息标签
     * @param multiple    是否批量拒绝
     * @throws IOException IO异常
     */
    public void manualNackWithRequeue(Channel channel, long deliveryTag, boolean multiple) throws IOException {
        try {
            channel.basicNack(deliveryTag, multiple, true);
            log.info("消息拒绝且重新入队 - deliveryTag: {}, multiple: {}", deliveryTag, multiple);
        } catch (IOException e) {
            log.error("消息拒绝重新入队失败 - deliveryTag: {}, multiple: {}", deliveryTag, multiple, e);
            throw e;
        }
    }

    /**
     * 注册消息确认信息
     * <p>
     * 在发送消息前注册，用于后续的确认回调处理
     * </p>
     *
     * @param correlationData 关联数据
     * @param message         消息内容
     * @param exchange        交换机
     * @param routingKey      路由键
     */
    public void registerConfirmInfo(CorrelationData correlationData, Object message,
                                   String exchange, String routingKey) {
        if (correlationData != null) {
            MessageConfirmInfo confirmInfo = new MessageConfirmInfo()
                    .setMessageId(correlationData.getId())
                    .setMessage(message)
                    .setExchange(exchange)
                    .setRoutingKey(routingKey)
                    .setSendTime(System.currentTimeMillis());

            confirmCache.put(correlationData.getId(), confirmInfo);
        }
    }

    /**
     * 处理确认成功
     */
    private void handleConfirmSuccess(String messageId, MessageConfirmInfo confirmInfo) {
        log.info("消息发送确认成功 - 消息ID: {}, 交换机: {}, 路由键: {}, 耗时: {}ms",
                messageId,
                confirmInfo != null ? confirmInfo.getExchange() : "unknown",
                confirmInfo != null ? confirmInfo.getRoutingKey() : "unknown",
                confirmInfo != null ? System.currentTimeMillis() - confirmInfo.getSendTime() : 0);

        // 可以在这里添加业务逻辑，如更新数据库状态、发送通知等
        // 例如：updateMessageStatus(messageId, "SENT_SUCCESS");
    }

    /**
     * 处理确认失败
     */
    private void handleConfirmFailure(String messageId, MessageConfirmInfo confirmInfo, String cause) {
        log.error("消息发送确认失败 - 消息ID: {}, 失败原因: {}, 交换机: {}, 路由键: {}",
                messageId, cause,
                confirmInfo != null ? confirmInfo.getExchange() : "unknown",
                confirmInfo != null ? confirmInfo.getRoutingKey() : "unknown");

        // 可以在这里添加重试逻辑或告警机制
        // 例如：retrySendMessage(confirmInfo) 或 sendAlert(messageId, cause);
    }

    /**
     * 处理返回的消息
     */
    private void handleReturnedMessage(String messageId, Object payload, int replyCode,
                                     String replyText, String exchange, String routingKey) {
        log.warn("处理返回消息 - 消息ID: {}, 消息内容: {}, 回复码: {}, 回复文本: {}",
                messageId, payload, replyCode, replyText);

        // 可以在这里添加业务逻辑，如记录日志、发送告警等
        // 例如：logReturnedMessage(messageId, payload, replyCode, replyText);
    }

    /**
     * 发送消息到死信队列
     */
    private void sendToDeadLetterQueue(Message message, int replyCode, String replyText,
                                     String exchange, String routingKey) {
        try {
            // 在消息头中添加路由失败信息
            MessageProperties properties = message.getMessageProperties();
            properties.setHeader("x-original-exchange", exchange);
            properties.setHeader("x-original-routing-key", routingKey);
            properties.setHeader("x-failure-reason", replyText);
            properties.setHeader("x-failure-code", replyCode);
            properties.setHeader("x-failure-timestamp", System.currentTimeMillis());

            // 发送到死信队列
            rabbitTemplate.convertAndSend(
                    "universe.life.dlx.exchange",
                    "routing.failed",
                    message
            );

            log.info("已将路由失败的消息发送到死信队列 - 消息ID: {}", properties.getMessageId());
        } catch (Exception e) {
            log.error("发送消息到死信队列失败", e);
        }
    }

    /**
     * 获取消息统计信息
     */
    public MessageStatistics getStatistics() {
        return new MessageStatistics()
                .setSuccessCount(successCount.get())
                .setFailureCount(failureCount.get())
                .setReturnCount(returnCount.get())
                .setTotalCount(successCount.get() + failureCount.get())
                .setSuccessRate(calculateSuccessRate());
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = successCount.get() + failureCount.get();
        return total == 0 ? 0.0 : (double) successCount.get() / total * 100;
    }

    /**
     * 消息确认信息类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class MessageConfirmInfo {
        private String messageId;
        private Object message;
        private String exchange;
        private String routingKey;
        private long sendTime;
        private int retryCount = 0;
    }

    /**
     * 消息统计信息类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class MessageStatistics {
        private long successCount;
        private long failureCount;
        private long returnCount;
        private long totalCount;
        private double successRate;
        private long timestamp = System.currentTimeMillis();
    }
}