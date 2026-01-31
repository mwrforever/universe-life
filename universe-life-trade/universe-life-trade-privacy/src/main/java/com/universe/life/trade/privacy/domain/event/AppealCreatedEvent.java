package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申诉创建事件
 * <p>
 * 当用户发起申诉时发布此事件。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申诉ID */
    private Long appealId;

    /** 订单ID */
    private Long orderId;

    /** 任务ID */
    private Long taskId;

    /** 申诉人ID */
    private Long appellantId;

    /** 申诉类型 */
    private Integer appealType;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
