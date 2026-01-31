package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 放弃任务命令
 */
@Data
@Builder
public class AbandonOrderCommand {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 操作用户ID（接单者）
     */
    private Long operatorId;

    /**
     * 放弃原因
     */
    private String reason;
}
