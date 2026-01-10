package com.universe.life.trade.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.trade.privacy.enums.TradeAppealResult;
import com.universe.life.trade.privacy.enums.TradeAppealStatus;
import com.universe.life.trade.privacy.enums.TradeAppealType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易申诉实体
 *
 * @author universe-life
 */
@Data
@TableName("trade_appeal")
public class TradeAppeal {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 交易订单ID
     */
    private Long orderId;

    /**
     * 申诉人ID
     */
    private Long appellantId;

    /**
     * 申诉类型
     */
    private TradeAppealType appealType;

    /**
     * 申诉原因
     */
    private String reason;

    /**
     * 证据图片URL（JSON数组）
     */
    private String evidenceImages;

    /**
     * 申诉状态
     */
    private TradeAppealStatus status;

    /**
     * 处理结果
     */
    private TradeAppealResult result;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
