package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单完整详情VO
 * 用于订单详情页展示，包含完整的订单信息
 */
@Data
@Schema(description = "交易订单完整详情信息")
public class TradeOrderFullDetailVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String taskTitle;

    @Schema(description = "任务描述")
    private String taskDescription;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "悬赏金额(元)")
    private String rewardAmountYuan;

    @Schema(description = "订单状态: 0=待审批, 1=进行中, 2=待确认, 3=待收款, 4=争议中, 5=付款中, 6=已完成, 7=已拒绝, 8=已放弃, 9=待评价")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusText;

    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;

    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "任务截止时间")
    private LocalDateTime taskDeadline;

    @Schema(description = "发布者信息")
    private UserInfoVO publisher;

    @Schema(description = "接单者信息")
    private UserInfoVO acceptor;

    @Schema(description = "提交成果")
    private SubmitResultVO submitResult;

    @Schema(description = "拒绝信息")
    private RejectInfoVO rejectInfo;

    @Schema(description = "申诉摘要")
    private AppealSummaryVO appealSummary;

    @Schema(description = "订单时间线")
    private List<OrderTimelineVO> timeline;

    @Schema(description = "是否是发布者")
    private Boolean isPublisher;

    @Schema(description = "是否是接单者")
    private Boolean isAcceptor;

    @Schema(description = "可用操作列表")
    private List<String> availableActions;
}
