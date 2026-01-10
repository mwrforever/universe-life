package com.universe.life.trade.privacy.domain.vo;

import com.universe.life.trade.privacy.enums.TradeOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单VO
 *
 * @author universe-life
 */
@Data
@Schema(description = "交易订单信息")
public class TradeOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "需求ID")
    private Long taskId;

    @Schema(description = "需求标题")
    private String taskTitle;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "接单者ID")
    private Long acceptorId;

    @Schema(description = "接单者名称")
    private String acceptorName;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "订单状态")
    private TradeOrderStatus status;

    @Schema(description = "提交内容")
    private String submitContent;

    @Schema(description = "提交图片列表")
    private List<String> submitImages;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;

    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;
}
