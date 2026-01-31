package com.universe.life.trade.privacy.infrastructure.mq;

import com.universe.life.trade.privacy.domain.event.OrderCompletedEvent;
import com.universe.life.trade.privacy.domain.event.OrderCreatedEvent;
import com.universe.life.trade.privacy.domain.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 交易事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 交易事件交换机
     */
    private static final String TRADE_EXCHANGE = "trade.exchange";

    /**
     * 订单创建路由键
     */
    private static final String ORDER_CREATED_ROUTING_KEY = "trade.order.created";

    /**
     * 订单状态变更路由键
     */
    private static final String ORDER_STATUS_CHANGED_ROUTING_KEY = "trade.order.status.changed";

    /**
     * 订单完成路由键
     */
    private static final String ORDER_COMPLETED_ROUTING_KEY = "trade.order.completed";

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    public void publish(Object event) {
        if (event instanceof OrderCreatedEvent) {
            publishOrderCreated((OrderCreatedEvent) event);
        } else if (event instanceof OrderStatusChangedEvent) {
            publishOrderStatusChanged((OrderStatusChangedEvent) event);
        } else if (event instanceof OrderCompletedEvent) {
            publishOrderCompleted((OrderCompletedEvent) event);
        } else {
            log.warn("未知的事件类型: {}", event.getClass().getName());
        }
    }

    /**
     * 发布订单创建事件
     */
    private void publishOrderCreated(OrderCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(TRADE_EXCHANGE, ORDER_CREATED_ROUTING_KEY, event);
            log.info("发布订单创建事件: orderId={}, taskId={}, acceptorId={}",
                    event.getOrderId(), event.getTaskId(), event.getAcceptorId());
        } catch (Exception e) {
            log.error("发布订单创建事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发布订单状态变更事件
     */
    private void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(TRADE_EXCHANGE, ORDER_STATUS_CHANGED_ROUTING_KEY, event);
            log.info("发布订单状态变更事件: orderId={}, from={}, to={}",
                    event.getOrderId(), event.getOldStatus(), event.getNewStatus());
        } catch (Exception e) {
            log.error("发布订单状态变更事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发布订单完成事件
     */
    private void publishOrderCompleted(OrderCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(TRADE_EXCHANGE, ORDER_COMPLETED_ROUTING_KEY, event);
            log.info("发布订单完成事件: orderId={}, taskId={}, acceptorId={}",
                    event.getOrderId(), event.getTaskId(), event.getAcceptorId());
        } catch (Exception e) {
            log.error("发布订单完成事件失败: {}", e.getMessage(), e);
        }
    }
}
