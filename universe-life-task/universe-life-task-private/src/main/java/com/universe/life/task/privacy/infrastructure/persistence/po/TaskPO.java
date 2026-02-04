package com.universe.life.task.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.infrastructure.enums.TaskDepositStatus;
import com.universe.life.task.privacy.infrastructure.enums.TaskReviewStatus;
import com.universe.life.task.privacy.infrastructure.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task 持久化对象
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
public class TaskPO {

    /** 任务ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long taskId;

    /** 任务标题 */
    @TableField("title")
    private String title;

    /** 任务描述 */
    @TableField("description")
    private String description;

    /** 悬赏金额（分） */
    @TableField("reward_amount")
    private Long rewardAmount;

    /** 保证金金额（分） */
    @TableField("deposit_amount")
    private Long depositAmount;

    /** 保证金状态 */
    @TableField("deposit_status")
    private TaskDepositStatus depositStatus;

    /** 分类ID */
    @TableField("category_id")
    private Long categoryId;

    /** 截止时间 */
    @TableField("deadline")
    private LocalDateTime deadline;

    /** 最大接单人数 */
    @TableField("max_acceptors")
    private Integer maxAcceptors;

    /** 当前接单人数 */
    @TableField("current_acceptors")
    private Integer currentAcceptors;

    /** 任务状态 */
    @TableField("status")
    private TaskStatus status;

    /** 审核状态 */
    @TableField("review_status")
    private TaskReviewStatus reviewStatus;

    /** 发布者ID */
    @TableField("publisher_id")
    private Long publisherId;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /** 乐观锁版本号 */
    @Version
    @TableField("version")
    private Long version;
}
