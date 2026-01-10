package com.universe.life.trade.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 拒绝接单请求
 *
 * @author universe-life
 */
@Data
@Schema(description = "拒绝接单请求")
public class TradeRejectRequest {

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 500, message = "拒绝原因不能超过500字")
    private String reason;
}
