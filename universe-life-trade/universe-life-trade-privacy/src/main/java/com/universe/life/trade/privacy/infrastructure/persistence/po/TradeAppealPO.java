package com.universe.life.trade.privacy.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.trade.privacy.enums.TradeAppealStatus;
import com.universe.life.trade.privacy.enums.TradeAppealType;
import com.universe.life.trade.privacy.enums.TradeAppealResult;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易申诉持久化对象
 * <p>
 * 对应数据库表 trade_appeal，用于 MyBatis Plus 持久化操作。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@TableName("trade_appeal")
public class TradeAppealPO {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 申诉人ID
     */
    @TableField("appellant_id")
    private Long appellantId;

    /**
     * 申诉类型
     */
    @TableField("appeal_type")
    private TradeAppealType appealType;

    /**
     * 申诉原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 证据图片URL（JSON数组）
     */
    @TableField("evidence_images")
    private String evidenceImages;

    /**
     * 申诉状态
     */
    @TableField("status")
    private TradeAppealStatus status;

    /**
     * 处理结果
     */
    @TableField("result")
    private TradeAppealResult result;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 处理时间
     */
    @TableField("handled_at")
    private LocalDateTime handledAt;

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
}
