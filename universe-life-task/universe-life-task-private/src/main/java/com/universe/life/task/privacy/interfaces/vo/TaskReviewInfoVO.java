package com.universe.life.task.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务审核记录VO
 */
@Data
@Schema(description = "任务审核记录")
public class TaskReviewInfoVO {

    @Schema(description = "审核记录ID")
    private Long reviewId;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核人名称")
    private String reviewerName;

    @Schema(description = "审核状态: 0=待审核, 1=通过, 2=拒绝")
    private Integer status;

    @Schema(description = "审核状态描述")
    private String statusText;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
}
