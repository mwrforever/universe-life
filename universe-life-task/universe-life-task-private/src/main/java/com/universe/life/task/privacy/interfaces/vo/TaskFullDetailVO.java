package com.universe.life.task.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务完整详情VO
 * 用于任务详情页展示，包含完整的任务信息
 */
@Data
@Schema(description = "任务完整详情信息")
public class TaskFullDetailVO {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "悬赏金额(元)")
    private String rewardAmountYuan;

    @Schema(description = "保证金金额(分)")
    private Long depositAmount;

    @Schema(description = "保证金金额(元)")
    private String depositAmountYuan;

    @Schema(description = "保证金状态: 0=待支付, 1=已支付, 2=已退还, 3=已结算")
    private Integer depositStatus;

    @Schema(description = "保证金状态描述")
    private String depositStatusText;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "截止时间描述")
    private String deadlineText;

    @Schema(description = "最大接单人数")
    private Integer maxAcceptors;

    @Schema(description = "当前接单人数")
    private Integer currentAcceptors;

    @Schema(description = "任务状态: 0=待审核, 1=审核拒绝, 2=招募中, 3=待支付, 4=支付中, 5=支付失败, 6=进行中, 7=已完成, 8=已取消, 9=已下架")
    private Integer status;

    @Schema(description = "任务状态描述")
    private String statusText;

    @Schema(description = "审核状态: 0=待审核, 1=通过, 2=拒绝")
    private Integer reviewStatus;

    @Schema(description = "审核状态描述")
    private String reviewStatusText;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "发布者信息")
    private PublisherInfoVO publisher;

    @Schema(description = "审核记录")
    private TaskReviewInfoVO reviewRecord;

    @Schema(description = "是否是任务发布者")
    private Boolean isOwner;

    @Schema(description = "是否可以申请接单")
    private Boolean canApply;

    @Schema(description = "当前用户是否已申请")
    private Boolean hasApplied;

    @Schema(description = "可用操作列表")
    private List<String> availableActions;
}
