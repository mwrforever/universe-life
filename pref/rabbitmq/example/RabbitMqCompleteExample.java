package com.universe.life.rabbitmq.example;

import com.universe.life.rabbitmq.callback.RabbitMqAckCallback;
import com.universe.life.rabbitmq.health.RabbitMqHealthChecker;
import com.universe.life.rabbitmq.model.*;
import com.universe.life.rabbitmq.service.RabbitMqReliableSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * RabbitMQ 完整使用示例
 * <p>
 * 展示 RabbitMQ 优化配置的完整使用方法，包括：
 * 1. 基础消息发送示例
 * 2. ACK 回调机制演示
 * 3. 消息重试机制演示
 * 4. 死信队列处理演示
 * 5. 延迟消息发送演示
 * 6. 批量消息发送演示
 * 7. 健康检查演示
 * 8. 性能监控演示
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rabbitmq.example.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMqCompleteExample implements CommandLineRunner {

    @Autowired
    private RabbitMqReliableSender reliableSender;

    @Autowired
    private RabbitMqAckCallback ackCallback;

    @Autowired
    private RabbitMqHealthChecker healthChecker;

    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始 RabbitMQ 完整示例演示 ==========");

        try {
            // 1. 健康检查
            performHealthCheck();

            // 2. 基础消息发送示例
            basicMessageExample();

            // 3. 用户消息示例
            userMessageExample();

            // 4. 订单消息示例
            orderMessageExample();

            // 5. 通知消息示例
            notificationMessageExample();

            // 6. 异步消息发送示例
            asyncMessageExample();

            // 7. 延迟消息示例
            delayMessageExample();

            // 8. 批量消息发送示例
            batchMessageExample();

            // 9. 事务消息示例
            transactionMessageExample();

            // 10. 消息统计示例
            messageStatisticsExample();

            // 11. 错误处理示例
            errorHandlingExample();

            log.info("========== RabbitMQ 完整示例演示完成 ==========");

        } catch (Exception e) {
            log.error("RabbitMQ 示例演示失败", e);
        }
    }

    /**
     * 健康检查示例
     */
    private void performHealthCheck() {
        log.info("\n=== 1. 健康检查示例 ===");

        try {
            // 执行健康检查
            var health = healthChecker.health();
            log.info("健康检查结果: {}", health.getStatus());

            // 获取详细健康状态
            var detailedStatus = healthChecker.getDetailedHealthStatus();
            log.info("详细健康状态:");
            log.info("  - 是否健康: {}", detailedStatus.isHealthy());
            log.info("  - 连接状态: {}", detailedStatus.getConnectionStatus());
            log.info("  - 队列数量: {}", detailedStatus.getQueueCount());
            log.info("  - 交换机数量: {}", detailedStatus.getExchangeCount());
            log.info("  - 总消息数: {}", detailedStatus.getTotalMessageCount());
            log.info("  - 成功率: {:.2f}%", detailedStatus.getSuccessRate());
            log.info("  - 最后检查时间: {}", detailedStatus.getLastCheckTime());

            if (!detailedStatus.getWarnings().isEmpty()) {
                log.warn("健康检查警告:");
                detailedStatus.getWarnings().forEach(warning -> log.warn("  - {}", warning));
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);
        }
    }

    /**
     * 基础消息发送示例
     */
    private void basicMessageExample() {
        log.info("\n=== 2. 基础消息发送示例 ===");

        try {
            // 创建基础消息
            RabbitMqBaseMessage baseMessage = new RabbitMqBaseMessage("TEST_MESSAGE", "这是一条测试消息")
                    .setSourceService("example-service")
                    .setPriority(2)
                    .setTtl(60000L);

            // 发送消息
            var result = reliableSender.sendReliableMessage(
                    "universe.life.main.exchange",
                    "test.routing.key",
                    baseMessage,
                    baseMessage.getPriority(),
                    baseMessage.getTtl()
            );

            log.info("基础消息发送结果:");
            log.info("  - 是否成功: {}", result.isSuccess());
            log.info("  - 消息ID: {}", result.getMessageId());
            log.info("  - 耗时: {}ms", result.getCostTime());

            if (!result.isSuccess()) {
                log.error("  - 失败原因: {}", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("基础消息发送失败", e);
        }
    }

    /**
     * 用户消息示例
     */
    private void userMessageExample() {
        log.info("\n=== 3. 用户消息示例 ===");

        try {
            // 创建用户注册消息
            UserMessage userRegisterMessage = UserMessage.createRegisterMessage(
                    1001L, "zhangsan", "zhangsan@example.com");

            // 发送用户注册消息
            var result1 = reliableSender.sendUserMessage(userRegisterMessage);
            log.info("用户注册消息发送结果: {}, 消息ID: {}", result1.isSuccess(), result1.getMessageId());

            // 创建用户状态变更消息
            UserMessage statusChangeMessage = UserMessage.createStatusChangeMessage(
                    1001L, 1, 2, "admin");

            // 发送用户状态变更消息
            var result2 = reliableSender.sendUserMessage(statusChangeMessage);
            log.info("用户状态变更消息发送结果: {}, 消息ID: {}", result2.isSuccess(), result2.getMessageId());

        } catch (Exception e) {
            log.error("用户消息发送失败", e);
        }
    }

    /**
     * 订单消息示例
     */
    private void orderMessageExample() {
        log.info("\n=== 4. 订单消息示例 ===");

        try {
            // 创建订单商品
            var orderItem = new OrderMessage.OrderItem()
                    .setProductId(2001L)
                    .setProductName("iPhone 15")
                    .setQuantity(1)
                    .setUnitPrice(new java.math.BigDecimal("7999.00"))
                    .setTotalPrice(new java.math.BigDecimal("7999.00"));

            // 创建订单创建消息
            OrderMessage orderMessage = OrderMessage.createOrderMessage(
                    3001L, "ORD20251110001", 1001L, new java.math.BigDecimal("7999.00"));
            orderMessage.setOrderItems(List.of(orderItem));
            orderMessage.setShippingAddress("北京市朝阳区XXX街道XXX号");
            orderMessage.setContactPhone("13800138000");

            // 发送订单消息
            var result = reliableSender.sendOrderMessage(orderMessage);
            log.info("订单消息发送结果: {}, 消息ID: {}", result.isSuccess(), result.getMessageId());

            // 创建订单支付成功消息
            OrderMessage paymentMessage = OrderMessage.createPaymentSuccessMessage(
                    3001L, "ORD20251110001", 1, LocalDateTime.now());

            var paymentResult = reliableSender.sendOrderMessage(paymentMessage);
            log.info("订单支付消息发送结果: {}, 消息ID: {}", paymentResult.isSuccess(), paymentResult.getMessageId());

        } catch (Exception e) {
            log.error("订单消息发送失败", e);
        }
    }

    /**
     * 通知消息示例
     */
    private void notificationMessageExample() {
        log.info("\n=== 5. 通知消息示例 ===");

        try {
            // 创建邮件通知
            NotificationMessage emailMessage = NotificationMessage.createEmailMessage(
                    1001L, "zhangsan@example.com",
                    "订单支付成功", "您的订单 ORD20251110001 已支付成功，感谢您的购买！");

            var emailResult = reliableSender.sendNotificationMessage(emailMessage);
            log.info("邮件通知发送结果: {}, 消息ID: {}", emailResult.isSuccess(), emailResult.getMessageId());

            // 创建短信通知
            NotificationMessage smsMessage = NotificationMessage.createSmsMessage(
                    1001L, "13800138000",
                    "【Universe Life】您的订单已支付成功，我们将尽快为您发货。");

            var smsResult = reliableSender.sendNotificationMessage(smsMessage);
            log.info("短信通知发送结果: {}, 消息ID: {}", smsResult.isSuccess(), smsResult.getMessageId());

            // 创建延迟通知（30秒后发送）
            NotificationMessage delayedMessage = NotificationMessage.createDelayedMessage(
                    "PUSH", 1001L, "订单发货提醒", "您的订单已发货，请注意查收",
                    LocalDateTime.now().plusSeconds(30));

            var delayedResult = reliableSender.sendNotificationMessage(delayedMessage);
            log.info("延迟通知发送结果: {}, 消息ID: {}", delayedResult.isSuccess(), delayedResult.getMessageId());

        } catch (Exception e) {
            log.error("通知消息发送失败", e);
        }
    }

    /**
     * 异步消息发送示例
     */
    private void asyncMessageExample() {
        log.info("\n=== 6. 异步消息发送示例 ===");

        try {
            // 创建多条消息
            List<UserMessage> messages = Arrays.asList(
                    UserMessage.createRegisterMessage(2001L, "user1", "user1@example.com"),
                    UserMessage.createRegisterMessage(2002L, "user2", "user2@example.com"),
                    UserMessage.createRegisterMessage(2003L, "user3", "user3@example.com")
            );

            // 异步发送消息
            List<CompletableFuture<RabbitMqReliableSender.SendResult>> futures = messages.stream()
                    .map(message -> reliableSender.sendReliableMessageAsync(
                            "universe.life.user.exchange",
                            "user.message",
                            message,
                            message.getPriority(),
                            message.getTtl()
                    ))
                    .toList();

            // 等待所有异步发送完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        log.info("所有异步消息发送完成");
                        futures.forEach(future -> {
                            try {
                                var result = future.get();
                                log.info("异步消息结果: {}, 消息ID: {}", result.isSuccess(), result.getMessageId());
                            } catch (Exception e) {
                                log.error("获取异步消息结果失败", e);
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        log.error("异步消息发送异常", throwable);
                        return null;
                    });

            log.info("异步消息发送已启动");

        } catch (Exception e) {
            log.error("异步消息发送失败", e);
        }
    }

    /**
     * 延迟消息示例
     */
    private void delayMessageExample() {
        log.info("\n=== 7. 延迟消息示例 ===");

        try {
            // 发送5秒延迟消息
            var delay5sResult = reliableSender.sendDelayMessage(
                    "这条消息将在5秒后到达",
                    "delay.processed",
                    5000L
            );
            log.info("5秒延迟消息发送结果: {}, 消息ID: {}", delay5sResult.isSuccess(), delay5sResult.getMessageId());

            // 发送30秒延迟消息
            var delay30sResult = reliableSender.sendDelayMessage(
                    "这条消息将在30秒后到达",
                    "delay.processed",
                    30000L
            );
            log.info("30秒延迟消息发送结果: {}, 消息ID: {}", delay30sResult.isSuccess(), delay30sResult.getMessageId());

            // 发送5分钟延迟消息
            var delay5mResult = reliableSender.sendDelayMessage(
                    "这条消息将在5分钟后到达",
                    "delay.processed",
                    300000L
            );
            log.info("5分钟延迟消息发送结果: {}, 消息ID: {}", delay5mResult.isSuccess(), delay5mResult.getMessageId());

        } catch (Exception e) {
            log.error("延迟消息发送失败", e);
        }
    }

    /**
     * 批量消息发送示例
     */
    private void batchMessageExample() {
        log.info("\n=== 8. 批量消息发送示例 ===");

        try {
            // 创建批量消息
            List<Object> messages = Arrays.asList(
                    UserMessage.createRegisterMessage(3001L, "batch1", "batch1@example.com"),
                    UserMessage.createRegisterMessage(3002L, "batch2", "batch2@example.com"),
                    UserMessage.createRegisterMessage(3003L, "batch3", "batch3@example.com"),
                    UserMessage.createRegisterMessage(3004L, "batch4", "batch4@example.com"),
                    UserMessage.createRegisterMessage(3005L, "batch5", "batch5@example.com")
            );

            // 批量发送消息
            var batchResult = reliableSender.sendBatchMessages(
                    "universe.life.user.exchange",
                    "user.message",
                    messages
            );

            log.info("批量消息发送结果:");
            log.info("  - 总数: {}", batchResult.getTotalCount());
            log.info("  - 成功: {}", batchResult.getSuccessCount());
            log.info("  - 失败: {}", batchResult.getFailureCount());
            log.info("  - 耗时: {}ms", batchResult.getCostTime());
            log.info("  - 成功率: {:.2f}%",
                    (double) batchResult.getSuccessCount() / batchResult.getTotalCount() * 100);

        } catch (Exception e) {
            log.error("批量消息发送失败", e);
        }
    }

    /**
     * 事务消息示例
     */
    private void transactionMessageExample() {
        log.info("\n=== 9. 事务消息示例 ===");

        try {
            // 创建事务消息
            RabbitMqBaseMessage transactionMessage = new RabbitMqBaseMessage(
                    "TRANSACTION_MESSAGE", "这是一条事务消息")
                    .setSourceService("transaction-service")
                    .setPriority(5);

            // 发送事务消息
            var result = reliableSender.sendTransactionalMessage(
                    "universe.life.main.exchange",
                    "transaction.routing.key",
                    transactionMessage
            );

            log.info("事务消息发送结果: {}, 消息ID: {}, 耗时: {}ms",
                    result.isSuccess(), result.getMessageId(), result.getCostTime());

        } catch (Exception e) {
            log.error("事务消息发送失败", e);
        }
    }

    /**
     * 消息统计示例
     */
    private void messageStatisticsExample() {
        log.info("\n=== 10. 消息统计示例 ===");

        try {
            // 获取发送统计
            var sendStatistics = reliableSender.getSendStatistics();
            log.info("消息发送统计:");
            log.info("  - 发送总数: {}", sendStatistics.getSendCount());
            log.info("  - 成功数: {}", sendStatistics.getSuccessCount());
            log.info("  - 失败数: {}", sendStatistics.getFailureCount());
            log.info("  - 成功率: {:.2f}%", sendStatistics.getSuccessRate());
            log.info("  - 统计时间: {}", new java.util.Date(sendStatistics.getTimestamp()));

            // 获取 ACK 回调统计
            var ackStatistics = ackCallback.getStatistics();
            log.info("ACK 回调统计:");
            log.info("  - 成功确认数: {}", ackStatistics.getSuccessCount());
            log.info("  - 失败确认数: {}", ackStatistics.getFailureCount());
            log.info("  - 返回消息数: {}", ackStatistics.getReturnCount());
            log.info("  - 总消息数: {}", ackStatistics.getTotalCount());
            log.info("  - 成功率: {:.2f}%", ackStatistics.getSuccessRate());
            log.info("  - 统计时间: {}", new java.util.Date(ackStatistics.getTimestamp()));

        } catch (Exception e) {
            log.error("获取消息统计失败", e);
        }
    }

    /**
     * 错误处理示例
     */
    private void errorHandlingExample() {
        log.info("\n=== 11. 错误处理示例 ===");

        try {
            // 尝试发送消息到不存在的交换机（模拟错误）
            RabbitMqBaseMessage errorMessage = new RabbitMqBaseMessage(
                    "ERROR_MESSAGE", "这是一条测试错误处理的消息")
                    .setSourceService("error-test-service");

            var errorResult = reliableSender.sendReliableMessage(
                    "nonexistent.exchange",
                    "error.routing.key",
                    errorMessage,
                    1,
                    60000L
            );

            log.info("错误消息发送结果: {}, 消息ID: {}, 错误信息: {}",
                    errorResult.isSuccess(), errorResult.getMessageId(), errorResult.getErrorMessage());

            // 尝试发送消息到不存在的队列（路由失败）
            var routingErrorResult = reliableSender.sendReliableMessage(
                    "universe.life.main.exchange",
                    "nonexistent.routing.key",
                    errorMessage,
                    1,
                    60000L
            );

            log.info("路由错误消息发送结果: {}, 消息ID: {}, 错误信息: {}",
                    routingErrorResult.isSuccess(), routingErrorResult.getMessageId(), routingErrorResult.getErrorMessage());

        } catch (Exception e) {
            log.error("错误处理示例失败", e);
        }
    }
}