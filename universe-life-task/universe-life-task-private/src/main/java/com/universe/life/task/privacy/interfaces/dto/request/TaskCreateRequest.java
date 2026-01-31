package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建任务请求
 */
@Data
@Schema(description = "创建任务请求")
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(min = 5, max = 100, message = "任务标题长度为5-100个字符")
    @Schema(description = "任务标题", example = "设计一个Logo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "任务描述不能为空")
    @Size(min = 20, max = 5000, message = "任务描述长度为20-5000个字符")
    @Schema(description = "任务描述", example = "需要设计一个简约风格的Logo...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "悬赏金额不能为空")
    @Min(value = 100, message = "悬赏金额最低1元")
    @Max(value = 10000000, message = "悬赏金额最高10万元")
    @Schema(description = "悬赏金额（分）", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rewardAmount;

    @NotNull(message = "任务分类不能为空")
    @Schema(description = "任务分类ID", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须是将来时间")
    @Schema(description = "截止时间", example = "2026-02-01T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime deadline;

    @NotNull(message = "最大接单人数不能为空")
    @Min(value = 1, message = "最大接单人数至少为1")
    @Max(value = 100, message = "最大接单人数最多为100")
    @Schema(description = "最大接单人数", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxAcceptors;
}
