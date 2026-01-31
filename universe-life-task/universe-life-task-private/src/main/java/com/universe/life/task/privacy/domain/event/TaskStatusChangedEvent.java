package com.universe.life.task.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务状态变更事件
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 旧状态 */
    private TaskStatus oldStatus;

    /** 新状态 */
    private TaskStatus newStatus;

    /** 变更时间 */
    private LocalDateTime changedAt;
}
