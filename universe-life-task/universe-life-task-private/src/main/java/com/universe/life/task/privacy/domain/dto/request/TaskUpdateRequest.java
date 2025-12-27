package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 编辑任务请求
 */
@Data
@Schema(description = "编辑任务请求")
public class TaskUpdateRequest {

    @Schema(description = "任务标题", example = "帮忙取快递")
    @Length(min = 2, max = 100, message = "任务标题长度为2-100字符")
    private String title;

    @Schema(description = "任务描述", example = "需要帮忙去菜鸟驿站取快递")
    private String description;

    @Schema(description = "悬赏金额(分)", example = "1000")
    @Min(value = 1, message = "悬赏金额必须大于0")
    private Long rewardAmount;

    @Schema(description = "任务分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "截止时间", example = "2024-12-31T23:59:59")
    @Future(message = "截止时间必须在将来")
    private LocalDateTime deadline;

    @Schema(description = "最大接受人数", example = "1")
    @Min(value = 1, message = "最大接受人数至少为1")
    private Integer maxAcceptors;
}
