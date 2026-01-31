package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单创建事件
 * <p>
 * 当用户申请接单时发布此事件，通知任务服务增加接单人数。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 任务ID */
    private Long taskId;

    /** 接单者ID */
    private Long acceptorId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
