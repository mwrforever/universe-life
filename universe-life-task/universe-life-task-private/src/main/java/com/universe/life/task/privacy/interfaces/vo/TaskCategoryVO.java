package com.universe.life.task.privacy.interfaces.vo;

import com.universe.life.task.privacy.enums.CommonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务分类VO
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "任务分类信息")
public class TaskCategoryVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private CommonStatus status;

    @Schema(description = "状态描述")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
