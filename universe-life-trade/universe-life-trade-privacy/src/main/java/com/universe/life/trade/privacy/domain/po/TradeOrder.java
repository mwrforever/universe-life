package com.universe.life.trade.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.trade.privacy.enums.TradeOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易订单实体
 *
 * @author universe-life
 */
@Data
@TableName("trade_order")
public class TradeOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 需求ID（关联task表）
     */
    private Long taskId;

    /**
     * 发布者ID（冗余存储）
     */
    private Long publisherId;

    /**
     * 接单者ID
     */
    private Long acceptorId;

    /**
     * 悬赏金额（分）
     */
    private Long rewardAmount;

    /**
     * 订单状态
     */
    private TradeOrderStatus status;

    /**
     * 提交内容
     */
    private String submitContent;

    /**
     * 提交图片URL（JSON数组）
     */
    private String submitImages;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 申请时间
     */
    private LocalDateTime appliedAt;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @Version
    private Long version;
}
