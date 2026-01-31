package com.universe.life.task.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaskReviewRecord 持久化对象
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_review_record")
public class TaskReviewRecordPO {

    /** 审核ID */
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    /** 任务ID */
    @TableField("task_id")
    private Long taskId;

    /** 审核人ID */
    @TableField("reviewer_id")
    private Long reviewerId;

    /** 审核状态 */
    @TableField("status")
    private TaskReviewStatus status;

    /** 拒绝原因 */
    @TableField("reject_reason")
    private String rejectReason;

    /** 审核时间 */
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
