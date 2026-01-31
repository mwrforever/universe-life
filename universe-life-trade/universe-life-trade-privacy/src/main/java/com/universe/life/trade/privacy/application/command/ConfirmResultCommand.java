package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 确认验收命令
 */
@Data
@Builder
public class ConfirmResultCommand {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 操作用户ID（发布者）
     */
    private Long operatorId;
}
