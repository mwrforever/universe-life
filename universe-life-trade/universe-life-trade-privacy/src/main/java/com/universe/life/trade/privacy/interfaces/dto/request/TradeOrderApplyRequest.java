package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请接单请求
 */
@Data
@Schema(description = "申请接单请求")
public class TradeOrderApplyRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @Schema(description = "申请备注", example = "我有丰富的设计经验，可以在3天内完成")
    private String remark;
}
