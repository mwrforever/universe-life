package com.universe.life.task.privacy.domain.vo;

import com.universe.life.task.privacy.enums.TaskReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务审核记录VO
 */
@Data
@Schema(description = "任务审核记录信息")
public class TaskReviewVO {

    @Schema(description = "审核记录ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核人名称")
    private String reviewerName;

    @Schema(description = "审核状态")
    private TaskReviewStatus status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
}
