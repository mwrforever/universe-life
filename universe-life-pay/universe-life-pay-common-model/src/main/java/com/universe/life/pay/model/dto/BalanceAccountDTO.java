package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户余额账户信息")
public class BalanceAccountDTO {

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "可用余额（分）")
    private Long availableBalance;

    @Schema(description = "冻结余额（分）")
    private Long frozenBalance;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
