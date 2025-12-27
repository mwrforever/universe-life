package com.universe.life.task.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.CommonStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务分类实体
 */
@Data
@TableName("task_category")
public class TaskCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String code;

    private Integer sort;

    private CommonStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
