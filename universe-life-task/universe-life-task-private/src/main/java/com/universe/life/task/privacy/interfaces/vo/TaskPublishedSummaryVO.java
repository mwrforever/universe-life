package com.universe.life.task.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我发布的任务摘要VO
 * 用于"我发布的任务"列表展示
 */
@Data
@Schema(description = "我发布的任务摘要信息")
public class TaskPublishedSummaryVO {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "悬赏金额(元)")
    private String rewardAmountYuan;

    @Schema(description = "任务状态: 0=待审核, 1=审核拒绝, 2=招募中, 3=待支付, 4=支付中, 5=支付失败, 6=进行中, 7=已完成, 8=已取消, 9=已下架")
    private Integer status;

    @Schema(description = "任务状态描述")
    private String statusText;

    @Schema(description = "当前接单人数")
    private Integer currentAcceptors;

    @Schema(description = "最大接单人数")
    private Integer maxAcceptors;

    @Schema(description = "待审批申请数")
    private Integer pendingApplicants;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
