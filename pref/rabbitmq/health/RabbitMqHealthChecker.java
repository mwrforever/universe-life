package com.universe.life.rabbitmq.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitMQ 健康检查器
 * <p>
 * 提供 RabbitMQ 连接和队列状态的健康检查，包括：
 * 1. 连接状态检查
 * 2. 队列状态检查
 * 3. 交换机状态检查
 * 4. 消息堆积监控
 * 5. 性能指标监控
 * 6. 异常告警机制
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Slf4j
@Component
public class RabbitMqHealthChecker implements HealthIndicator {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * 健康检查结果缓存
     */
    private final ConcurrentHashMap<String, HealthCheckResult> healthCache = new ConcurrentHashMap<>();

    /**
     * 检查计数器
     */
    private final AtomicLong checkCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);

    @Override
    public Health health() {
        try {
            HealthCheckResult result = performHealthCheck();

            if (result.isHealthy()) {
                successCount.incrementAndGet();
                return Health.up()
                        .withDetails("status", "UP")
                        .withDetails("connection", "CONNECTED")
                        .withDetails("queues", result.getQueueCount())
                        .withDetails("exchanges", result.getExchangeCount())
                        .withDetails("checkTime", result.getCheckTime())
                        .withDetails("lastCheck", LocalDateTime.now())
                        .build();
            } else {
                failureCount.incrementAndGet();
                return Health.down()
                        .withDetails("status", "DOWN")
                        .withDetails("reason", result.getErrorMessage())
                        .withDetails("connection", result.getConnectionStatus())
                        .withDetails("checkTime", result.getCheckTime())
                        .withDetails("lastCheck", LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            log.error("RabbitMQ 健康检查异常", e);

            return Health.down()
                    .withDetails("status", "DOWN")
                    .withDetails("reason", e.getMessage())
                    .withDetails("checkTime", System.currentTimeMillis())
                    .withDetails("lastCheck", LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 执行健康检查
     *
     * @return 健康检查结果
     */
    public HealthCheckResult performHealthCheck() {
        HealthCheckResult result = new HealthCheckResult();
        result.setCheckTime(System.currentTimeMillis());
        checkCount.incrementAndGet();

        try {
            // 1. 检查连接状态
            Connection connection = checkConnection();
            result.setConnectionStatus("CONNECTED");

            // 2. 检查关键队列
            int queueCount = checkQueues(connection);
            result.setQueueCount(queueCount);

            // 3. 检查关键交换机
            int exchangeCount = checkExchanges();
            result.setExchangeCount(exchangeCount);

            // 4. 检查消息堆积情况
            checkMessageBacklog(connection, result);

            result.setHealthy(true);
            log.debug("RabbitMQ 健康检查通过 - 队列数: {}, 交换机数: {}", queueCount, exchangeCount);

        } catch (Exception e) {
            result.setHealthy(false);
            result.setErrorMessage(e.getMessage());
            result.setConnectionStatus("DISCONNECTED");
            log.error("RabbitMQ 健康检查失败", e);
        }

        return result;
    }

    /**
     * 检查连接状态
     *
     * @return 连接对象
     * @throws Exception 连接异常
     */
    private Connection checkConnection() throws Exception {
        try {
            Connection connection = connectionFactory.createConnection();
            if (connection != null && connection.isOpen()) {
                log.debug("RabbitMQ 连接正常");
                return connection;
            } else {
                throw new Exception("无法创建连接或连接已关闭");
            }
        } catch (Exception e) {
            throw new Exception("连接检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查关键队列
     *
     * @param connection 连接对象
     * @return 队列数量
     * @throws Exception 检查异常
     */
    private int checkQueues(Connection connection) throws Exception {
        try (var channel = connection.createChannel()) {
            int queueCount = 0;

            // 检查关键队列是否存在
            String[] criticalQueues = {
                "universe.life.user.queue",
                "universe.life.order.queue",
                "universe.life.notification.queue",
                "universe.life.dlx.queue"
            };

            for (String queueName : criticalQueues) {
                try {
                    channel.queueDeclarePassive(queueName);
                    queueCount++;
                    log.debug("队列检查通过: {}", queueName);
                } catch (Exception e) {
                    log.warn("队列检查失败: {}, 原因: {}", queueName, e.getMessage());
                    throw new Exception("关键队列不存在: " + queueName);
                }
            }

            return queueCount;
        }
    }

    /**
     * 检查关键交换机
     *
     * @return 交换机数量
     * @throws Exception 检查异常
     */
    private int checkExchanges() throws Exception {
        try {
            int exchangeCount = 0;

            // 检查关键交换机是否存在
            String[] criticalExchanges = {
                "universe.life.main.exchange",
                "universe.life.user.exchange",
                "universe.life.order.exchange",
                "universe.life.notification.exchange",
                "universe.life.dlx.exchange"
            };

            for (String exchangeName : criticalExchanges) {
                try {
                    rabbitAdmin.getRabbitTemplate().execute(channel -> {
                        channel.exchangeDeclarePassive(exchangeName);
                        return null;
                    });
                    exchangeCount++;
                    log.debug("交换机检查通过: {}", exchangeName);
                } catch (Exception e) {
                    log.warn("交换机检查失败: {}, 原因: {}", exchangeName, e.getMessage());
                    throw new Exception("关键交换机不存在: " + exchangeName);
                }
            }

            return exchangeCount;
        } catch (Exception e) {
            throw new Exception("交换机检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查消息堆积情况
     *
     * @param connection 连接对象
     * @param result      健康检查结果
     */
    private void checkMessageBacklog(Connection connection, HealthCheckResult result) {
        try (var channel = connection.createChannel()) {

            // 检查关键队列的消息数量
            String[] queuesToCheck = {
                "universe.life.user.queue",
                "universe.life.order.queue",
                "universe.life.notification.queue"
            };

            long totalMessageCount = 0;
            long maxQueueDepth = 0;

            for (String queueName : queuesToCheck) {
                try {
                    var declareOk = channel.queueDeclarePassive(queueName);
                    long messageCount = declareOk.getMessageCount();
                    totalMessageCount += messageCount;
                    maxQueueDepth = Math.max(maxQueueDepth, messageCount);

                    // 检查是否超过阈值
                    if (messageCount > 1000) {
                        log.warn("队列消息堆积过多 - 队列: {}, 消息数: {}", queueName, messageCount);
                        result.addWarning("队列 " + queueName + " 消息堆积: " + messageCount);
                    }

                } catch (Exception e) {
                    log.warn("检查队列消息数量失败: {}, 原因: {}", queueName, e.getMessage());
                }
            }

            result.setTotalMessageCount(totalMessageCount);
            result.setMaxQueueDepth(maxQueueDepth);

            // 如果总消息数过多，设置警告
            if (totalMessageCount > 5000) {
                result.addWarning("总消息堆积过多: " + totalMessageCount);
            }

        } catch (Exception e) {
            log.warn("检查消息堆积情况失败", e);
            result.addWarning("无法检查消息堆积情况: " + e.getMessage());
        }
    }

    /**
     * 获取详细健康状态
     *
     * @return 详细健康状态
     */
    public DetailedHealthStatus getDetailedHealthStatus() {
        DetailedHealthStatus status = new DetailedHealthStatus();
        status.setCheckCount(checkCount.get());
        status.setSuccessCount(successCount.get());
        status.setFailureCount(failureCount.get());
        status.setSuccessRate(calculateSuccessRate());
        status.setLastCheckTime(LocalDateTime.now());

        try {
            HealthCheckResult result = performHealthCheck();
            status.setHealthy(result.isHealthy());
            status.setConnectionStatus(result.getConnectionStatus());
            status.setQueueCount(result.getQueueCount());
            status.setExchangeCount(result.getExchangeCount());
            status.setTotalMessageCount(result.getTotalMessageCount());
            status.setMaxQueueDepth(result.getMaxQueueDepth());
            status.setWarnings(result.getWarnings());
            status.setErrorMessage(result.getErrorMessage());
        } catch (Exception e) {
            status.setHealthy(false);
            status.setErrorMessage(e.getMessage());
        }

        return status;
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        checkCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        log.info("RabbitMQ 健康检查统计数据已重置");
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = checkCount.get();
        return total == 0 ? 0.0 : (double) successCount.get() / total * 100;
    }

    /**
     * 健康检查结果类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class HealthCheckResult {
        private boolean healthy = true;
        private String connectionStatus;
        private String errorMessage;
        private long checkTime;
        private int queueCount;
        private int exchangeCount;
        private long totalMessageCount;
        private long maxQueueDepth;
        private java.util.List<String> warnings = new java.util.ArrayList<>();

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    /**
     * 详细健康状态类
     */
    @lombok.Data
    @lombok.experimental.Accessors(chain = true)
    public static class DetailedHealthStatus {
        private boolean healthy;
        private long checkCount;
        private long successCount;
        private long failureCount;
        private double successRate;
        private String connectionStatus;
        private int queueCount;
        private int exchangeCount;
        private long totalMessageCount;
        private long maxQueueDepth;
        private java.util.List<String> warnings = new java.util.ArrayList<>();
        private String errorMessage;
        private LocalDateTime lastCheckTime;
    }
}