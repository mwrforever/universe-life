package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建支付DTO（服务间调用）")
public class CreatePaymentDTO {

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizType;

    @Schema(description = "业务ID（如订单ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bizId;

    @Schema(description = "支付请求号（幂等键）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestNo;

    @Schema(description = "付款方用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long payerId;

    @Schema(description = "收款方用户ID")
    private Long payeeId;

    @Schema(description = "支付金额（分）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long amount;

    @Schema(description = "支付渠道：0微信 1支付宝 2银行卡", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer channel;

    @Schema(description = "扩展信息（JSON字符串）")
    private String extra;
}
