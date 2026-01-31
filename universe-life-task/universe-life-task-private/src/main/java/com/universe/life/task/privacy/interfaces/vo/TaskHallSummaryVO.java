package com.universe.life.task.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务大厅摘要VO
 * 用于任务大厅列表展示，只包含必要的摘要信息
 */
@Data
@Schema(description = "任务大厅摘要信息")
public class TaskHallSummaryVO {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "悬赏金额(元)")
    private String rewardAmountYuan;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "截止时间描述")
    private String deadlineText;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者昵称")
    private String publisherNickname;

    @Schema(description = "发布者头像")
    private String publisherAvatar;

    @Schema(description = "当前接单人数")
    private Integer currentAcceptors;

    @Schema(description = "最大接单人数")
    private Integer maxAcceptors;

    @Schema(description = "接单进度(如: 2/5)")
    private String acceptorProgress;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
