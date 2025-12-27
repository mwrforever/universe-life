package com.universe.life.task.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.TaskDepositStatus;
import com.universe.life.task.privacy.enums.TaskReviewStatus;
import com.universe.life.task.privacy.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体
 */
@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long publisherId;

    private String title;

    private String description;

    private Long rewardAmount;

    private Long depositAmount;

    private TaskDepositStatus depositStatus;

    private Long categoryId;

    private LocalDateTime deadline;

    private Integer maxAcceptors;

    private Integer currentAcceptors;

    private TaskStatus status;

    private TaskReviewStatus reviewStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}
