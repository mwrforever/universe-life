package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付保证金请求
 */
@Data
@Schema(description = "支付保证金请求")
public class TaskDepositRequest {

    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALIPAY")
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
