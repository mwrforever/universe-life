package com.universe.life.trade.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发起申诉请求
 *
 * @author universe-life
 */
@Data
@Schema(description = "发起申诉请求")
public class TradeAppealRequest {

    @Schema(description = "申诉原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "申诉原因不能为空")
    private String reason;

    @Schema(description = "证据图片URL列表")
    @Size(max = 9, message = "图片数量不能超过9张")
    private List<String> evidenceImages;
}
