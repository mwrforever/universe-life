package com.universe.life.trade.privacy.application.event;

import com.universe.life.aftercare.model.event.AppealCreatedEvent;
import com.universe.life.aftercare.model.enums.AftercareBizType;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.infrastructure.constants.RabbitMqConstants;
import com.universe.life.trade.privacy.infrastructure.constants.RedisKeyConstants;
import com.universe.life.trade.privacy.infrastructure.mq.TradeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppealEventListener {

    private final TradeOrderRepository orderRepository;
    private final TradeEventPublisher eventPublisher;
    private final CacheUtil cacheUtil;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = RabbitMqConstants.Queue.APPEAL_CREATED_QUEUE,
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                            type = ExchangeTypes.TOPIC
                    ),
                    key = RabbitMqConstants.RoutingKey.APPEAL_CREATED
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleAppealCreated(AppealCreatedEvent event) {
        if (event == null || event.getBizId() == null || event.getBizType() == null) {
            return;
        }

        if (!AftercareBizType.ORDER.equals(AftercareBizType.of(event.getBizType()))) {
            return;
        }

        Long orderId = event.getBizId();
        log.info("收到申诉创建事件: appealId={}, bizType={}, bizId={}", event.getAppealId(), event.getBizType(), event.getBizId());

        TradeOrderAggregate order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("订单不存在，忽略申诉创建事件: orderId={}", orderId);
            return;
        }

        boolean locked = orderRepository.lockForAppeal(orderId);
        if (!locked) {
            log.info("订单已处于申诉锁定状态: orderId={}", orderId);
        }

        TradeOrderAggregate refreshed = orderRepository.findById(orderId).orElse(null);
        if (refreshed == null) {
            return;
        }

        if (refreshed.getStatus() != null && refreshed.getStatus().canDispute()) {
            refreshed.dispute();
            orderRepository.save(refreshed);
            refreshed.pullDomainEvents().forEach(eventPublisher::publish);
        } else {
            log.warn("订单状态不允许进入争议中，跳过状态变更: orderId={}, status={}", orderId,
                    refreshed.getStatus() == null ? null : refreshed.getStatus().name());
        }

        String cacheKey = RedisKeyConstants.buildOrderDetailKey(orderId);
        cacheUtil.delete(cacheKey);
    }
}
