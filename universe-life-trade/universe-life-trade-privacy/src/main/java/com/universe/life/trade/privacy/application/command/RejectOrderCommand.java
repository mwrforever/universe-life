package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 拒绝接单命令
 */
@Data
@Builder
public class RejectOrderCommand {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 操作用户ID（发布者）
     */
    private Long operatorId;

    /**
     * 拒绝原因
     */
    private String reason;
}
