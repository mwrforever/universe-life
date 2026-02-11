package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端余额流水")
public class AdminBalanceFlowDTO {

    @Schema(description = "流水号")
    private String flowNo;

    @Schema(description = "请求号（已脱敏）")
    private String requestNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "动作")
    private String action;

    @Schema(description = "金额（分）")
    private Long amount;

    @Schema(description = "可用余额变化")
    private Long availableDelta;

    @Schema(description = "冻结余额变化")
    private Long frozenDelta;

    @Schema(description = "变更后可用余额")
    private Long balanceAfter;

    @Schema(description = "变更后冻结余额")
    private Long frozenAfter;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
