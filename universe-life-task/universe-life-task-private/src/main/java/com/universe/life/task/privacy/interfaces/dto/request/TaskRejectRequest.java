package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 任务审核拒绝请求
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "任务审核拒绝请求")
public class TaskRejectRequest {

    @NotBlank(message = "拒绝原因不能为空")
    @Size(min = 5, max = 500, message = "拒绝原因长度为5-500个字符")
    @Schema(description = "拒绝原因", example = "任务描述不清晰，请补充详细信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
