package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 强制下架请求
 */
@Data
@Schema(description = "强制下架请求")
public class TaskOfflineRequest {

    @Schema(description = "下架原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "任务内容违规")
    @NotBlank(message = "下架原因不能为空")
    private String reason;
}
