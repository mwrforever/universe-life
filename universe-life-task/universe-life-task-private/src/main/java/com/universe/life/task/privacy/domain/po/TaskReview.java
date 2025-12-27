package com.universe.life.task.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.TaskReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务审核记录实体
 */
@Data
@TableName("task_review")
public class TaskReview {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long reviewerId;

    private TaskReviewStatus status;

    private String rejectReason;

    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
