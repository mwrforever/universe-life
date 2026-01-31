package com.universe.life.task.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务审核拒绝事件
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRejectedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 发布者ID */
    private Long publisherId;

    /** 审核人ID */
    private Long reviewerId;

    /** 拒绝原因 */
    private String rejectReason;

    /** 审核时间 */
    private LocalDateTime rejectedAt;
}
