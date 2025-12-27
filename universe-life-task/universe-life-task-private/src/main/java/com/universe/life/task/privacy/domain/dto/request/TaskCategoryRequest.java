package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务分类请求
 */
@Data
@Schema(description = "任务分类请求")
public class TaskCategoryRequest {

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "跑腿代办")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ERRAND")
    @NotBlank(message = "分类编码不能为空")
    private String code;

    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态(0-禁用,1-启用)", example = "1")
    private Integer status;
}
