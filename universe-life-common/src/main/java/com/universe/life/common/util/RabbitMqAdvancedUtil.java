package com.universe.life.common.util;

import cn.hutool.core.util.StrUtil;
import com.universe.life.common.config.RabbitMqConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RabbitMQ高级功能工具类
 * <p>
 * 提供RabbitMQ的高级功能支持，包括死信队列、延迟队列、优先级队列等高级特性的实现
 * 支持通过YAML配置文件进行自动配置，简化高级功能的部署和使用
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>死信队列自动配置和管理</li>
 *   <li>延迟队列（基于插件）支持</li>
 *   <li>优先级队列支持</li>
 *   <li>TTL队列自动创建</li>
 *   <li>消息过期处理</li>
 *   <li>队列镜像配置</li>
 *   <li>消息备份队列</li>
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
public class RabbitMqAdvancedUtil {

    private final AmqpAdmin amqpAdmin;
    private final RabbitMqConfigProperties configProperties;

    /**
     * 创建死信队列
     * <p>
     * 死信队列用于处理无法正常消费的消息，当消息在主队列中过期、被拒绝或队列满时，
     * 消息会被自动转发到死信队列进行后续处理
     *
     * @param mainQueueName    主队列名称
     * @param deadQueueName    死信队列名称
     * @param deadExchangeName 死信交换机名称
     * @param routingKey       路由键
     * @return 主队列实例
     */
    public Queue createDeadLetterQueue(String mainQueueName, String deadQueueName,
                                       String deadExchangeName, String routingKey) {
        try {
            // 创建死信交换机
            DirectExchange deadLetterExchange = ExchangeBuilder
                    .directExchange(deadExchangeName)
                    .durable(true)
                    .build();
            amqpAdmin.declareExchange(deadLetterExchange);

            // 创建死信队列
            Queue deadLetterQueue = QueueBuilder
                    .durable(deadQueueName)
                    .build();
            amqpAdmin.declareQueue(deadLetterQueue);

            // 绑定死信队列到死信交换机
            Binding deadLetterBinding = BindingBuilder
                    .bind(deadLetterQueue)
                    .to(deadLetterExchange)
                    .with(routingKey);
            amqpAdmin.declareBinding(deadLetterBinding);

            // 创建主队列，并配置死信队列参数
            Map<String, Object> mainQueueArgs = new HashMap<>();
            mainQueueArgs.put("x-dead-letter-exchange", deadExchangeName);
            mainQueueArgs.put("x-dead-letter-routing-key", routingKey);
            mainQueueArgs.put("x-message-ttl", 60000); // 60秒TTL

            Queue mainQueue = QueueBuilder
                    .durable(mainQueueName)
                    .withArguments(mainQueueArgs)
                    .build();
            amqpAdmin.declareQueue(mainQueue);

            log.info("死信队列创建成功 - 主队列: {}, 死信队列: {}, 死信交换机: {}",
                    mainQueueName, deadQueueName, deadExchangeName);

            return mainQueue;

        } catch (Exception e) {
            log.error("死信队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("死信队列创建失败", e);
        }
    }

    /**
     * 基于配置创建死信队列
     *
     * @param deadLetterConfig 死信队列配置
     * @return 主队列实例
     */
    public Queue createDeadLetterQueue(RabbitMqConfigProperties.DeadLetterConfig deadLetterConfig) {
        if (deadLetterConfig == null || StrUtil.isBlank(deadLetterConfig.getMainQueue())) {
            throw new IllegalArgumentException("死信队列配置不能为空");
        }

        return createDeadLetterQueue(
                deadLetterConfig.getMainQueue(),
                deadLetterConfig.getDeadLetterQueue(),
                deadLetterConfig.getDeadLetterExchange(),
                deadLetterConfig.getDeadLetterRoutingKey()
        );
    }

    /**
     * 创建延迟队列
     * <p>
     * 延迟队列用于延迟消息的处理，消息会先发送到延迟队列，经过指定的延迟时间后，
     * 才会被转发到目标队列。需要安装rabbitmq_delayed_message_exchange插件
     *
     * @param delayQueueName    延迟队列名称
     * @param targetQueueName   目标队列名称
     * @param delayExchangeName 延迟交换机名称
     * @param targetExchangeName 目标交换机名称
     * @param routingKey        路由键
     * @return 延迟队列实例
     */
    public Queue createDelayQueue(String delayQueueName, String targetQueueName,
                                  String delayExchangeName, String targetExchangeName,
                                  String routingKey) {
        try {
            // 创建延迟交换机（需要插件支持）
            CustomExchange delayExchange = new CustomExchange(
                    delayExchangeName,
                    "x-delayed-message",
                    true,
                    false,
                    Map.of("x-delayed-type", "direct")
            );
            amqpAdmin.declareExchange(delayExchange);

            // 创建目标交换机
            DirectExchange targetExchange = ExchangeBuilder
                    .directExchange(targetExchangeName)
                    .durable(true)
                    .build();
            amqpAdmin.declareExchange(targetExchange);

            // 创建目标队列
            Queue targetQueue = QueueBuilder
                    .durable(targetQueueName)
                    .build();
            amqpAdmin.declareQueue(targetQueue);

            // 绑定目标队列到目标交换机
            Binding targetBinding = BindingBuilder
                    .bind(targetQueue)
                    .to(targetExchange)
                    .with(routingKey);
            amqpAdmin.declareBinding(targetBinding);

            // 创建延迟队列
            Queue delayQueue = QueueBuilder
                    .durable(delayQueueName)
                    .withArgument("x-dead-letter-exchange", targetExchangeName)
                    .withArgument("x-dead-letter-routing-key", routingKey)
                    .build();
            amqpAdmin.declareQueue(delayQueue);

            // 绑定延迟队列到延迟交换机
            Binding delayBinding = BindingBuilder
                    .bind(delayQueue)
                    .to(delayExchange)
                    .with(routingKey);
            amqpAdmin.declareBinding(delayBinding);

            log.info("延迟队列创建成功 - 延迟队列: {}, 目标队列: {}, 延迟交换机: {}",
                    delayQueueName, targetQueueName, delayExchangeName);

            return delayQueue;

        } catch (Exception e) {
            log.error("延迟队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("延迟队列创建失败", e);
        }
    }

    /**
     * 基于配置创建延迟队列
     *
     * @param delayConfig 延迟队列配置
     * @return 延迟队列实例
     */
    public Queue createDelayQueue(RabbitMqConfigProperties.DelayConfig delayConfig) {
        if (delayConfig == null || StrUtil.isBlank(delayConfig.getDelayQueue())) {
            throw new IllegalArgumentException("延迟队列配置不能为空");
        }

        return createDelayQueue(
                delayConfig.getDelayQueue(),
                delayConfig.getTargetQueue(),
                delayConfig.getDelayExchange(),
                delayConfig.getTargetExchange(),
                delayConfig.getRoutingKey()
        );
    }

    /**
     * 创建优先级队列
     * <p>
     * 优先级队列支持根据消息的优先级进行消费，高优先级的消息会优先被处理。
     * 优先级范围是0-255，数值越大优先级越高
     *
     * @param queueName 队列名称
     * @param maxPriority 最大优先级
     * @return 优先级队列实例
     */
    public Queue createPriorityQueue(String queueName, int maxPriority) {
        try {
            if (maxPriority < 1 || maxPriority > 255) {
                throw new IllegalArgumentException("优先级范围必须在1-255之间");
            }

            Map<String, Object> args = new HashMap<>();
            args.put("x-max-priority", maxPriority);

            Queue priorityQueue = QueueBuilder
                    .durable(queueName)
                    .withArguments(args)
                    .build();

            amqpAdmin.declareQueue(priorityQueue);

            log.info("优先级队列创建成功 - 队列: {}, 最大优先级: {}", queueName, maxPriority);
            return priorityQueue;

        } catch (Exception e) {
            log.error("优先级队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("优先级队列创建失败", e);
        }
    }

    /**
     * 创建TTL队列
     * <p>
     * TTL队列中的消息会在指定时间后自动过期并被删除或转移到死信队列
     *
     * @param queueName 队列名称
     * @param ttl       TTL时间（毫秒）
     * @return TTL队列实例
     */
    public Queue createTtlQueue(String queueName, long ttl) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", ttl);

            Queue ttlQueue = QueueBuilder
                    .durable(queueName)
                    .withArguments(args)
                    .build();

            amqpAdmin.declareQueue(ttlQueue);

            log.info("TTL队列创建成功 - 队列: {}, TTL: {}ms", queueName, ttl);
            return ttlQueue;

        } catch (Exception e) {
            log.error("TTL队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("TTL队列创建失败", e);
        }
    }

    /**
     * 创建队列镜像
     * <p>
     * 队列镜像实现高可用，当主节点故障时，镜像节点会自动接管
     *
     * @param queueName      队列名称
     * @param haPolicy       高可用策略
     * @param haSyncMode     同步模式
     * @return 镜像队列实例
     */
    public Queue createMirroredQueue(String queueName, String haPolicy, String haSyncMode) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x-ha-policy", haPolicy != null ? haPolicy : "all");

            if (StrUtil.isNotBlank(haSyncMode)) {
                args.put("x-ha-sync-mode", haSyncMode);
            }

            Queue mirroredQueue = QueueBuilder
                    .durable(queueName)
                    .withArguments(args)
                    .build();

            amqpAdmin.declareQueue(mirroredQueue);

            log.info("镜像队列创建成功 - 队列: {}, HA策略: {}, 同步模式: {}", queueName, haPolicy, haSyncMode);
            return mirroredQueue;

        } catch (Exception e) {
            log.error("镜像队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("镜像队列创建失败", e);
        }
    }

    /**
     * 创建备份队列
     * <p>
     * 备份队列用于消息备份，主队列中的消息会同时发送到备份队列
     *
     * @param mainQueueName    主队列名称
     * @param backupQueueName  备份队列名称
     * @param backupExchangeName 备份交换机名称
     * @return 主队列实例
     */
    public Queue createBackupQueue(String mainQueueName, String backupQueueName, String backupExchangeName) {
        try {
            // 创建备份交换机
            FanoutExchange backupExchange = ExchangeBuilder
                    .fanoutExchange(backupExchangeName)
                    .durable(true)
                    .build();
            amqpAdmin.declareExchange(backupExchange);

            // 创建备份队列
            Queue backupQueue = QueueBuilder
                    .durable(backupQueueName)
                    .build();
            amqpAdmin.declareQueue(backupQueue);

            // 绑定备份队列到备份交换机
            Binding backupBinding = BindingBuilder
                    .bind(backupQueue)
                    .to(backupExchange);
            amqpAdmin.declareBinding(backupBinding);

            // 创建主队列，并配置备份交换机
            Map<String, Object> mainQueueArgs = new HashMap<>();
            mainQueueArgs.put("x-alternate-exchange", backupExchangeName);

            Queue mainQueue = QueueBuilder
                    .durable(mainQueueName)
                    .withArguments(mainQueueArgs)
                    .build();
            amqpAdmin.declareQueue(mainQueue);

            log.info("备份队列创建成功 - 主队列: {}, 备份队列: {}, 备份交换机: {}",
                    mainQueueName, backupQueueName, backupExchangeName);

            return mainQueue;

        } catch (Exception e) {
            log.error("备份队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("备份队列创建失败", e);
        }
    }

    /**
     * 创建过期队列
     * <p>
     * 过期队列在指定时间后会被自动删除，适用于临时队列场景
     *
     * @param queueName 队列名称
     * @param ttl       队列TTL时间（毫秒）
     * @return 过期队列实例
     */
    public Queue createExpiringQueue(String queueName, long ttl) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x-expires", ttl);

            Queue expiringQueue = QueueBuilder
                    .durable(queueName)
                    .withArguments(args)
                    .build();

            amqpAdmin.declareQueue(expiringQueue);

            log.info("过期队列创建成功 - 队列: {}, 队列TTL: {}ms", queueName, ttl);
            return expiringQueue;

        } catch (Exception e) {
            log.error("过期队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("过期队列创建失败", e);
        }
    }

    /**
     * 创建限流队列
     * <p>
     * 限流队列限制消息的进入速率，防止系统过载
     *
     * @param queueName     队列名称
     * @param maxLength     最大消息数量
     * @param overflowBehavior 溢出行为（drop-head, reject-publish, reject-publish-dlx）
     * @return 限流队列实例
     */
    public Queue createLimitedQueue(String queueName, int maxLength, String overflowBehavior) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x-max-length", maxLength);

            if (StrUtil.isNotBlank(overflowBehavior)) {
                args.put("x-overflow", overflowBehavior);
            }

            Queue limitedQueue = QueueBuilder
                    .durable(queueName)
                    .withArguments(args)
                    .build();

            amqpAdmin.declareQueue(limitedQueue);

            log.info("限流队列创建成功 - 队列: {}, 最大长度: {}, 溢出行为: {}",
                    queueName, maxLength, overflowBehavior);
            return limitedQueue;

        } catch (Exception e) {
            log.error("限流队列创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("限流队列创建失败", e);
        }
    }

    /**
     * 自动配置所有高级队列
     * <p>
     * 根据配置文件自动创建死信队列、延迟队列等高级功能队列
     */
    public void autoConfigureAdvancedQueues() {
        try {
            // 自动配置死信队列
            if (configProperties.getDeadLetter() != null) {
                createDeadLetterQueue(configProperties.getDeadLetter());
            }

            // 自动配置延迟队列
            if (configProperties.getDelay() != null) {
                createDelayQueue(configProperties.getDelay());
            }

            // 自动配置优先级队列
            if (configProperties.getQueues() != null) {
                RabbitMqConfigProperties.QueueConfig priorityQueueConfig =
                        configProperties.getQueues().get("priority");
                if (priorityQueueConfig != null && priorityQueueConfig.getArguments() != null) {
                    Object maxPriority = priorityQueueConfig.getArguments().get("max-priority");
                    if (maxPriority != null) {
                        createPriorityQueue(priorityQueueConfig.getName(), (Integer) maxPriority);
                    }
                }

                // 自动配置TTL队列
                RabbitMqConfigProperties.QueueConfig ttlQueueConfig =
                        configProperties.getQueues().get("ttl");
                if (ttlQueueConfig != null && ttlQueueConfig.getArguments() != null) {
                    Object messageTtl = ttlQueueConfig.getArguments().get("message-ttl");
                    if (messageTtl != null) {
                        createTtlQueue(ttlQueueConfig.getName(), (Long) messageTtl);
                    }
                }
            }

            log.info("高级队列自动配置完成");

        } catch (Exception e) {
            log.error("高级队列自动配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("高级队列自动配置失败", e);
        }
    }
}