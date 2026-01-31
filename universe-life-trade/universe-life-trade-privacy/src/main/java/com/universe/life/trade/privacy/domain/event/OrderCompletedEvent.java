package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单完成事件
 * <p>
 * 当订单完成时发布此事件，通知任务服务检查是否所有订单都已完成。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 任务ID */
    private Long taskId;

    /** 接单者ID */
    private Long acceptorId;

    /** 完成时间 */
    private LocalDateTime completedAt;
}
