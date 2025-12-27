package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 发布任务请求
 */
@Data
@Schema(description = "发布任务请求")
public class TaskCreateRequest {

    @Schema(description = "任务标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "帮忙取快递")
    @NotBlank(message = "任务标题不能为空")
    @Length(min = 2, max = 100, message = "任务标题长度为2-100字符")
    private String title;

    @Schema(description = "任务描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "需要帮忙去菜鸟驿站取快递")
    @NotBlank(message = "任务描述不能为空")
    private String description;

    @Schema(description = "悬赏金额(分)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "悬赏金额不能为空")
    @Min(value = 1, message = "悬赏金额必须大于0")
    private Long rewardAmount;

    @Schema(description = "任务分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务分类不能为空")
    private Long categoryId;

    @Schema(description = "截止时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12-31T23:59:59")
    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须在将来")
    private LocalDateTime deadline;

    @Schema(description = "最大接受人数", example = "1", defaultValue = "1")
    @Min(value = 1, message = "最大接受人数至少为1")
    private Integer maxAcceptors = 1;
}
