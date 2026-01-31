package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新任务分类请求
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "更新任务分类请求")
public class UpdateCategoryRequest {

    @Size(min = 2, max = 20, message = "分类名称长度为2-20个字符")
    @Schema(description = "分类名称", example = "设计类")
    private String name;

    @Size(max = 200, message = "分类描述最多200个字符")
    @Schema(description = "分类描述", example = "包含Logo设计、UI设计等")
    private String description;

    @Schema(description = "排序值（越小越靠前）", example = "1")
    private Integer sortOrder;
}
