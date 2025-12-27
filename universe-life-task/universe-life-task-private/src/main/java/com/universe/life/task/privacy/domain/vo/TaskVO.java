package com.universe.life.task.privacy.domain.vo;

import com.universe.life.task.privacy.enums.TaskDepositStatus;
import com.universe.life.task.privacy.enums.TaskReviewStatus;
import com.universe.life.task.privacy.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务VO
 */
@Data
@Schema(description = "任务信息")
public class TaskVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "保证金金额(分)")
    private Long depositAmount;

    @Schema(description = "保证金状态")
    private TaskDepositStatus depositStatus;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "最大接受人数")
    private Integer maxAcceptors;

    @Schema(description = "当前接受人数")
    private Integer currentAcceptors;

    @Schema(description = "任务状态")
    private TaskStatus status;

    @Schema(description = "审核状态")
    private TaskReviewStatus reviewStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
