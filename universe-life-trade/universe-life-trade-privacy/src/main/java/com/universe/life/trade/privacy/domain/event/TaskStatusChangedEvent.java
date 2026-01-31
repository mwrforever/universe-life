package com.universe.life.trade.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务状态变更事件
 * <p>
 * 从任务服务接收的事件，用于同步任务状态快照。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 旧状态
     */
    private String oldStatus;

    /**
     * 新状态
     */
    private String newStatus;

    /**
     * 变更时间
     */
    private LocalDateTime changedAt;
}
