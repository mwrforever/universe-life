package com.universe.life.trade.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易订单持久化对象
 * <p>
 * 对应数据库表 trade_order，用于 MyBatis Plus 持久化操作。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@TableName("trade_order")
public class TradeOrderPO {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 发布者ID（冗余存储）
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 接单者ID
     */
    @TableField("acceptor_id")
    private Long acceptorId;

    /**
     * 悬赏金额（分，冗余存储）
     */
    @TableField("reward_amount")
    private Long rewardAmount;

    /**
     * 订单状态
     */
    @TableField("status")
    private TradeOrderStatusEnum status;

    /**
     * 提交内容
     */
    @TableField("submit_content")
    private String submitContent;

    /**
     * 提交图片URL（JSON数组）
     */
    @TableField("submit_images")
    private String submitImages;

    /**
     * 拒绝原因
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 拒绝时间
     */
    @TableField("rejected_at")
    private LocalDateTime rejectedAt;

    /**
     * 拒绝人ID
     */
    @TableField("rejected_by")
    private Long rejectedBy;

    /**
     * 申请时间
     */
    @TableField("applied_at")
    private LocalDateTime appliedAt;

    /**
     * 审批时间
     */
    @TableField("approved_at")
    private LocalDateTime approvedAt;

    /**
     * 提交时间
     */
    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记：0未删除 1已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Long version;
}
