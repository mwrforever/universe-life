package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新任务请求
 */
@Data
@Schema(description = "更新任务请求")
public class TaskUpdateRequest {

    @Size(min = 5, max = 100, message = "任务标题长度为5-100个字符")
    @Schema(description = "任务标题", example = "设计一个Logo")
    private String title;

    @Size(min = 20, max = 5000, message = "任务描述长度为20-5000个字符")
    @Schema(description = "任务描述", example = "需要设计一个简约风格的Logo...")
    private String description;

    @Min(value = 100, message = "悬赏金额最低1元")
    @Max(value = 10000000, message = "悬赏金额最高10万元")
    @Schema(description = "悬赏金额（分）", example = "50000")
    private Long rewardAmount;

    @Schema(description = "任务分类ID", example = "4")
    private Long categoryId;

    @Future(message = "截止时间必须是将来时间")
    @Schema(description = "截止时间", example = "2026-02-01T18:00:00")
    private LocalDateTime deadline;

    @Min(value = 1, message = "最大接单人数至少为1")
    @Max(value = 100, message = "最大接单人数最多为100")
    @Schema(description = "最大接单人数", example = "5")
    private Integer maxAcceptors;
}
