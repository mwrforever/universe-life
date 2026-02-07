package com.universe.life.aftercare.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "处理申诉请求")
public class HandleAppealRequest {

    @NotNull(message = "处理结果不能为空")
    @Schema(description = "处理结果: 1=支持申诉方, 2=驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer result;

    @Schema(description = "处理备注")
    private String handleRemark;

     @Schema(description = "裁决后的应付金额（分，可选）")
     private Long payableAmountCents;

     @Schema(description = "已支付金额（分，可选）")
     private Long paidAmountCents;
}
