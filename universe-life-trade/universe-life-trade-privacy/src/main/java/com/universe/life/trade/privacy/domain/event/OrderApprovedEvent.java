package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单审批通过事件
 * <p>
 * 当发布者同意接单申请时发布此事件，通知任务服务更新状态。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderApprovedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 任务ID */
    private Long taskId;

    /** 接单者ID */
    private Long acceptorId;

    /** 审批时间 */
    private LocalDateTime approvedAt;
}
