package com.universe.life.trade.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建交易订单请求（申请接单）
 *
 * @author universe-life
 */
@Data
@Schema(description = "创建交易订单请求")
public class TradeOrderCreateRequest {

    @Schema(description = "需求ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求ID不能为空")
    private Long taskId;
}
