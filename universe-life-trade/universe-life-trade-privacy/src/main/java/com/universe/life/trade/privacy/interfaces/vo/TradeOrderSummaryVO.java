package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易订单摘要VO
 * 用于"我的接单"列表展示，只包含必要的摘要信息
 */
@Data
@Schema(description = "交易订单摘要信息")
public class TradeOrderSummaryVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String taskTitle;

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

    @Schema(description = "任务截止时间")
    private LocalDateTime taskDeadline;

    @Schema(description = "截止时间描述")
    private String deadlineText;

    @Schema(description = "发布者昵称")
    private String publisherNickname;

    @Schema(description = "发布者头像")
    private String publisherAvatar;
}
