package com.universe.life.task.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.TaskAcceptanceStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务接受记录实体
 */
@Data
@TableName("task_acceptance")
public class TaskAcceptance {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long acceptorId;

    private TaskAcceptanceStatus status;

    private String submitContent;

    private String submitImages;

    private LocalDateTime acceptedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}
