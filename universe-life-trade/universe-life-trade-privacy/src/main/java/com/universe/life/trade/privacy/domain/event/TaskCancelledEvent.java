package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务取消事件
 * <p>
 * 从任务服务接收的事件，用于取消相关订单。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;
}
