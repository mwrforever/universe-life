package com.universe.life.task.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付保证金响应VO
 */
@Data
@Schema(description = "支付保证金响应")
public class TaskDepositVO {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "支付链接")
    private String paymentUrl;
}
