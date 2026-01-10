package com.universe.life.trade.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认验收请求
 *
 * @author universe-life
 */
@Data
@Schema(description = "确认验收请求")
public class TradeConfirmRequest {

    @Schema(description = "是否确认通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "确认结果不能为空")
    private Boolean confirmed;
}
