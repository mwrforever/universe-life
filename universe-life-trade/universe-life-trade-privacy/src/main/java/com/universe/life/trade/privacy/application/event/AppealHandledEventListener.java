package com.universe.life.trade.privacy.application.event;

import com.universe.life.aftercare.model.event.AppealHandledEvent;
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
public class AppealHandledEventListener {

    private final TradeOrderRepository orderRepository;
    private final TradeEventPublisher eventPublisher;
    private final CacheUtil cacheUtil;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = RabbitMqConstants.Queue.APPEAL_HANDLED_QUEUE,
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = RabbitMqConstants.Exchange.TRADE_EXCHANGE,
                            type = ExchangeTypes.TOPIC
                    ),
                    key = RabbitMqConstants.RoutingKey.APPEAL_HANDLED
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleAppealHandled(AppealHandledEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }

        Long orderId = event.getOrderId();
        String dedupKey = buildDedupKey(event);
        if (!cacheUtil.setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS)) {
            log.info("申诉处理事件重复消费(幂等跳过): appealId={}, orderId={}, result={}, dedupKey={}",
                    event.getAppealId(), orderId, event.getResult(), dedupKey);
            return;
        }

        log.info("收到申诉处理事件: appealId={}, orderId={}, result={}, dedupKey={}",
                event.getAppealId(), orderId, event.getResult(), dedupKey);

        try {
            boolean unlocked = orderRepository.unlockForAppeal(orderId);
            if (!unlocked) {
                log.info("订单未处于申诉锁定状态或已解锁: orderId={}", orderId);
            }

            TradeOrderAggregate order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("订单不存在，忽略申诉处理事件: orderId={}", orderId);
                return;
            }

            if (order.getStatus() != TradeOrderStatusEnum.DISPUTE) {
                log.info("订单状态无需处理申诉回流(幂等跳过): orderId={}, status={}", orderId,
                        order.getStatus() == null ? null : order.getStatus().name());
                return;
            }

            if (event.getPayableAmountCents() != null) {
                order.updatePayableAmountCents(event.getPayableAmountCents());
            }
            if (event.getPaidAmountCents() != null) {
                order.updatePaidAmountCents(event.getPaidAmountCents());
            }

            resolveDisputeStatus(order, event);
            orderRepository.save(order);
            order.pullDomainEvents().forEach(eventPublisher::publish);
        } finally {
            String cacheKey = RedisKeyConstants.buildOrderDetailKey(orderId);
            cacheUtil.delete(cacheKey);
        }
    }

    private String buildDedupKey(AppealHandledEvent event) {
        String handledAt = event.getHandledAt() == null ? "" : event.getHandledAt().toString();
        return "trade:mq:dedup:appeal:handled:order:" + event.getOrderId()
                + ":appeal:" + event.getAppealId()
                + ":at:" + handledAt;
    }

    private void resolveDisputeStatus(TradeOrderAggregate order, AppealHandledEvent event) {
        Integer result = event.getResult();
        Long appellantId = event.getAppellantId();

        if (result == null || appellantId == null) {
            order.complete();
            return;
        }

        boolean appellantIsAcceptor = appellantId.equals(order.getAcceptorId());

        if (result == 1) {
            // 支持申诉方
            if (appellantIsAcceptor) {
                order.resolveDisputeToPayment();
            } else {
                order.resolveDisputeToProgress();
            }
            return;
        }

        if (result == 2) {
            // 驳回申诉：支持对方
            if (appellantIsAcceptor) {
                order.resolveDisputeToProgress();
            } else {
                order.resolveDisputeToPayment();
            }
            return;
        }

        order.complete();
    }
}
