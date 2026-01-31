package com.universe.life.task.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单完成事件（来自交易服务）
 * 
 * @author Universe Life Team
 * @since 2025-01-18
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

    /** 接单人ID */
    private Long acceptorId;

    /** 完成时间 */
    private LocalDateTime completedAt;
}
