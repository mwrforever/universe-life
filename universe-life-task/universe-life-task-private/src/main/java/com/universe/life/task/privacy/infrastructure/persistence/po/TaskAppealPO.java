package com.universe.life.task.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.TaskAppealResult;
import com.universe.life.task.privacy.enums.TaskAppealStatus;
import com.universe.life.task.privacy.enums.TaskAppealType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务申诉持久化对象
 */
@Data
@TableName("task_appeal")
public class TaskAppealPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long acceptanceId;

    private Long appellantId;

    private TaskAppealType appealType;

    private String reason;

    private String evidenceImages;

    private TaskAppealStatus status;

    private TaskAppealResult result;

    private Long handlerId;

    private String handleRemark;

    private LocalDateTime handledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
