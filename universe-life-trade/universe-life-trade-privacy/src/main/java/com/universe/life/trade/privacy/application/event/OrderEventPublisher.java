package com.universe.life.trade.privacy.application.event;

import com.universe.life.trade.privacy.domain.event.*;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.infrastructure.constants.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单事件发布器
 * <p>
 * 负责发布订单相关的领域事件到 RabbitMQ，供任务服务消费。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布订单创建事件
     */
    public void publishOrderCreated(TradeOrderAggregate order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getIdValue())
                .taskId(order.getTaskId())
                .acceptorId(order.getAcceptorId())
                .createdAt(order.getAppliedAt())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                RabbitMqConstants.RoutingKey.ORDER_CREATED,
                event
        );
        
        log.info("发布订单创建事件: orderId={}, taskId={}, acceptorId={}", 
                order.getIdValue(), order.getTaskId(), order.getAcceptorId());
    }

    /**
     * 发布订单审批通过事件
     */
    public void publishOrderApproved(TradeOrderAggregate order) {
        OrderApprovedEvent event = OrderApprovedEvent.builder()
                .orderId(order.getIdValue())
                .taskId(order.getTaskId())
                .acceptorId(order.getAcceptorId())
                .approvedAt(order.getApprovedAt())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                RabbitMqConstants.RoutingKey.ORDER_APPROVED,
                event
        );
        
        log.info("发布订单审批通过事件: orderId={}, taskId={}", 
                order.getIdValue(), order.getTaskId());
    }

    /**
     * 发布订单审批拒绝事件
     */
    public void publishOrderRejected(TradeOrderAggregate order, String rejectReason) {
        OrderRejectedEvent event = OrderRejectedEvent.builder()
                .orderId(order.getIdValue())
                .taskId(order.getTaskId())
                .acceptorId(order.getAcceptorId())
                .rejectReason(rejectReason)
                .rejectedAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                RabbitMqConstants.RoutingKey.ORDER_REJECTED,
                event
        );
        
        log.info("发布订单审批拒绝事件: orderId={}, taskId={}, reason={}", 
                order.getIdValue(), order.getTaskId(), rejectReason);
    }

    /**
     * 发布订单状态变更事件
     */
    public void publishOrderStatusChanged(TradeOrderAggregate order) {
        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .orderId(order.getIdValue())
                .taskId(order.getTaskId())
                .oldStatus(null) // TODO: 需要在Order中记录旧状态
                .newStatus(order.getStatus())
                .changedAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                RabbitMqConstants.RoutingKey.ORDER_STATUS_CHANGED,
                event
        );
        
        log.info("发布订单状态变更事件: orderId={}, status={}", 
                order.getIdValue(), order.getStatus());
    }

    /**
     * 发布订单完成事件
     */
    public void publishOrderCompleted(TradeOrderAggregate order) {
        OrderCompletedEvent event = OrderCompletedEvent.builder()
                .orderId(order.getIdValue())
                .taskId(order.getTaskId())
                .acceptorId(order.getAcceptorId())
                .completedAt(order.getCompletedAt())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                RabbitMqConstants.RoutingKey.ORDER_COMPLETED,
                event
        );
        
        log.info("发布订单完成事件: orderId={}, taskId={}", 
                order.getIdValue(), order.getTaskId());
    }
}
