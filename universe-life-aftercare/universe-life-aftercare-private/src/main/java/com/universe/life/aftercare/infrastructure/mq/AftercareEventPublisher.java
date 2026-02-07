package com.universe.life.aftercare.infrastructure.mq;

import com.universe.life.aftercare.model.event.AppealCancelledEvent;
import com.universe.life.aftercare.model.event.AppealCreatedEvent;
import com.universe.life.aftercare.model.event.AppealHandledEvent;
import com.universe.life.common.util.RabbitMqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AftercareEventPublisher {

    private final RabbitMqSender rabbitMqSender;

    private static final String TRADE_EXCHANGE = "trade.exchange";

    private static final String APPEAL_CREATED_ROUTING_KEY = "appeal.created";

    private static final String APPEAL_HANDLED_ROUTING_KEY = "appeal.handled";

    private static final String APPEAL_CANCELLED_ROUTING_KEY = "appeal.cancelled";

    public void publishAppealCreated(AppealCreatedEvent event) {
        try {
            rabbitMqSender.builder()
                    .to(TRADE_EXCHANGE, APPEAL_CREATED_ROUTING_KEY)
                    .persistent(true)
                    .send(event);
            log.info("发布申诉创建事件: appealId={}, orderId={}", event.getAppealId(), event.getOrderId());
        } catch (Exception e) {
            log.error("发布申诉创建事件失败: {}", e.getMessage(), e);
        }
    }

    public void publishAppealHandled(AppealHandledEvent event) {
        try {
            rabbitMqSender.builder()
                    .to(TRADE_EXCHANGE, APPEAL_HANDLED_ROUTING_KEY)
                    .persistent(true)
                    .send(event);
            log.info("发布申诉处理事件: appealId={}, orderId={}, result={}", event.getAppealId(), event.getOrderId(), event.getResult());
        } catch (Exception e) {
            log.error("发布申诉处理事件失败: {}", e.getMessage(), e);
        }
    }

    public void publishAppealCancelled(AppealCancelledEvent event) {
        try {
            rabbitMqSender.builder()
                    .to(TRADE_EXCHANGE, APPEAL_CANCELLED_ROUTING_KEY)
                    .persistent(true)
                    .send(event);
            log.info("发布申诉撤销事件: appealId={}, orderId={}", event.getAppealId(), event.getOrderId());
        } catch (Exception e) {
            log.error("发布申诉撤销事件失败: {}", e.getMessage(), e);
        }
    }
}
