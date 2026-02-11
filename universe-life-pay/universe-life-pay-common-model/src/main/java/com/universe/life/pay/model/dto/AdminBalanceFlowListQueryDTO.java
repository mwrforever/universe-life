package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端余额流水分页查询参数")
public class AdminBalanceFlowListQueryDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "动作")
    private String action;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "页大小", defaultValue = "20")
    private Integer size = 20;
}
