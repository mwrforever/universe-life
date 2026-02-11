package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户端支付记录")
public class UserPayRecordDTO {

    @Schema(description = "支付记录ID")
    private Long id;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "支付请求号（已脱敏）")
    private String requestNo;

    @Schema(description = "支付金额（分）")
    private Long amount;

    @Schema(description = "渠道")
    private Integer channel;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "第三方交易号（已脱敏）")
    private String thirdTradeNo;

    @Schema(description = "第三方预支付ID")
    private String thirdPrepayId;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
