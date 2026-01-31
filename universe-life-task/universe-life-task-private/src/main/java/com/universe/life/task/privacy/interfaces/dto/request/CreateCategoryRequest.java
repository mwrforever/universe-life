package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建任务分类请求
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "创建任务分类请求")
public class CreateCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 20, message = "分类名称长度为2-20个字符")
    @Schema(description = "分类名称", example = "设计类", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "分类编码不能为空")
    @Size(min = 2, max = 50, message = "分类编码长度为2-50个字符")
    @Schema(description = "分类编码", example = "DESIGN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 200, message = "分类描述最多200个字符")
    @Schema(description = "分类描述", example = "包含Logo设计、UI设计等")
    private String description;

    @NotNull(message = "排序值不能为空")
    @Schema(description = "排序值（越小越靠前）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortOrder;
}
