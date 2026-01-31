package com.universe.life.trade.privacy.domain.event;

import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单状态变更事件
 * <p>
 * 当订单状态发生变化时发布此事件。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 任务ID */
    private Long taskId;

    /** 旧状态 */
    private TradeOrderStatusEnum oldStatus;

    /** 新状态 */
    private TradeOrderStatusEnum newStatus;

    /** 变更时间 */
    private LocalDateTime changedAt;
}
