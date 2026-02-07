package com.universe.life.trade.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发起争议（申诉冻结订单）DTO")
public class DisputeOrderDTO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "操作者ID（发布者或接单者）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorId;
}
