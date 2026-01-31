package com.universe.life.task.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.task.privacy.enums.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaskCategory 持久化对象
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_category")
public class TaskCategoryPO {

    /** 分类ID */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /** 分类名称 */
    @TableField("name")
    private String name;

    /** 分类代码 */
    @TableField("code")
    private String code;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    /** 状态 */
    @TableField("status")
    private CommonStatus status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
