package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认任务完成请求
 */
@Data
@Schema(description = "确认任务完成请求")
public class TaskConfirmRequest {

    @Schema(description = "是否确认完成", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "确认状态不能为空")
    private Boolean confirmed;

    @Schema(description = "备注", example = "任务完成质量很好")
    private String remark;
}
