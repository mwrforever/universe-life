package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核拒绝请求
 */
@Data
@Schema(description = "审核拒绝请求")
public class TaskRejectRequest {

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "任务描述不清晰")
    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
}
