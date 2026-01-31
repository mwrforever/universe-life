package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 拒绝接单请求
 * <p>
 * 发布者拒绝接单申请时使用，必须提供拒绝原因。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "拒绝接单请求")
public class TradeRejectRequest {

    /**
     * 拒绝原因
     * <p>
     * 必填，长度5-500个字符。
     * 用于告知接单者被拒绝的原因。
     * </p>
     */
    @NotBlank(message = "拒绝原因不能为空")
    @Size(min = 5, max = 500, message = "拒绝原因长度为5-500个字符")
    @Schema(description = "拒绝原因", example = "抱歉，您的经验不符合要求", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
