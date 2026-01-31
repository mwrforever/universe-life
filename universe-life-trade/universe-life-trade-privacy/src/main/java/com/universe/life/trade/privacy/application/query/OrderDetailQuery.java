package com.universe.life.trade.privacy.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 订单详情查询
 */
@Data
@Builder
public class OrderDetailQuery {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 当前用户ID
     */
    private Long currentUserId;
}
