package com.universe.life.trade.privacy.application.event;

import com.universe.life.aftercare.model.event.AppealCancelledEvent;
import com.universe.life.aftercare.model.enums.AftercareBizType;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.infrastructure.constants.RabbitMqConstants;
import com.universe.life.trade.privacy.infrastructure.constants.RedisKeyConstants;
import com.universe.life.trade.privacy.infrastructure.mq.TradeEventPublisher;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppealCancelledEventListener {

    private final TradeOrderRepository orderRepository;
    private final TradeEventPublisher eventPublisher;
    private final CacheUtil cacheUtil;

    private String buildDedupKey(AppealCancelledEvent event) {
        String cancelledAt = event.getCancelledAt() == null ? "" : event.getCancelledAt().toString();
        return "trade:mq:dedup:appeal:cancelled:order:" + event.getBizId()
                + ":appeal:" + event.getAppealId()
                + ":at:" + cancelledAt;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = RabbitMqConstants.Queue.APPEAL_CANCELLED_QUEUE,
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                            type = ExchangeTypes.TOPIC
                    ),
                    key = RabbitMqConstants.RoutingKey.APPEAL_CANCELLED
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleAppealCancelled(AppealCancelledEvent event) {
        if (event == null || event.getBizId() == null || event.getBizType() == null) {
            return;
        }

        if (!AftercareBizType.ORDER.equals(AftercareBizType.of(event.getBizType()))) {
            return;
        }

        Long orderId = event.getBizId();
        String dedupKey = buildDedupKey(event);
        if (!cacheUtil.setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS)) {
            log.info("申诉撤销事件重复消费(幂等跳过): appealId={}, orderId={}, dedupKey={}",
                    event.getAppealId(), orderId, dedupKey);
            return;
        }

        log.info("收到申诉撤销事件: appealId={}, orderId={}, dedupKey={}", event.getAppealId(), orderId, dedupKey);

        try {
            boolean unlocked = orderRepository.unlockForAppeal(orderId);
            if (!unlocked) {
                log.info("订单未处于申诉锁定状态或已解锁: orderId={}", orderId);
            }

            TradeOrderAggregate order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("订单不存在，忽略申诉撤销事件: orderId={}", orderId);
                return;
            }

            if (order.getStatus() != TradeOrderStatusEnum.DISPUTE) {
                log.info("订单状态无需处理申诉撤销(幂等跳过): orderId={}, status={}", orderId,
                        order.getStatus() == null ? null : order.getStatus().name());
                return;
            }

            order.restoreStatusFromPreAppeal();
            orderRepository.save(order);
            order.pullDomainEvents().forEach(eventPublisher::publish);
        } finally {
            String cacheKey = RedisKeyConstants.buildOrderDetailKey(orderId);
            cacheUtil.delete(cacheKey);
        }
    }
}
