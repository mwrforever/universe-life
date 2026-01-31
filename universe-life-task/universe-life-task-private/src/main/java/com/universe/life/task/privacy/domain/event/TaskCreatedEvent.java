package com.universe.life.task.privacy.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务创建事件
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 发布者ID */
    private Long publisherId;

    /** 悬赏金额（分） */
    private Long rewardAmount;

    /** 分类ID */
    private Long categoryId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
