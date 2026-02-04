package com.universe.life.task.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.infrastructure.enums.TaskReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务审核记录持久化对象
 */
@Data
@TableName("task_review")
public class TaskReviewPO {

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
