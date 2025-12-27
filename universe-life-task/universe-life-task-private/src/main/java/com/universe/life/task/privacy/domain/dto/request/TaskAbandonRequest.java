package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 放弃任务请求
 */
@Data
@Schema(description = "放弃任务请求")
public class TaskAbandonRequest {

    @Schema(description = "放弃原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间冲突无法完成")
    @NotBlank(message = "放弃原因不能为空")
    private String reason;
}
