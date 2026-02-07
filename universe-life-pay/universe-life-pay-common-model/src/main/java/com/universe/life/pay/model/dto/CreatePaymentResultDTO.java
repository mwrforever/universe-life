package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建支付结果DTO")
public class CreatePaymentResultDTO {

    @Schema(description = "支付记录ID")
    private Long payRecordId;

    @Schema(description = "支付请求号")
    private String requestNo;

    @Schema(description = "支付渠道")
    private Integer channel;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "第三方交易号")
    private String thirdTradeNo;

    @Schema(description = "第三方预支付ID")
    private String thirdPrepayId;

    @Schema(description = "支付参数（JSON字符串）")
    private String payParams;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
