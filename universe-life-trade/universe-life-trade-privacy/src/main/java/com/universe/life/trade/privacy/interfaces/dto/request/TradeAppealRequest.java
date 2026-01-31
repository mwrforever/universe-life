package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发起申诉请求
 * <p>
 * 用户对订单结果发起申诉时使用。
 * 发布者和接单者都可以发起申诉。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "发起申诉请求")
public class TradeAppealRequest {

    /**
     * 申诉类型
     * <p>
     * 必填，可选值：
     * <ul>
     *   <li>1 - 成果不符合要求（发布者申诉）</li>
     *   <li>2 - 发布者恶意拒绝（接单者申诉）</li>
     *   <li>3 - 其他</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "申诉类型不能为空")
    @Schema(description = "申诉类型: 1-成果不符合要求, 2-发布者恶意拒绝, 3-其他", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer appealType;

    /**
     * 申诉原因
     * <p>
     * 必填，长度20-1000个字符。
     * 详细描述申诉的原因和诉求。
     * </p>
     */
    @NotBlank(message = "申诉原因不能为空")
    @Size(min = 20, max = 1000, message = "申诉原因长度为20-1000个字符")
    @Schema(description = "申诉原因", example = "发布者无故拒绝验收，成果完全符合要求", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    /**
     * 证据图片URL列表
     * <p>
     * 可选，最多9张图片。
     * 用于提供申诉的证据材料。
     * </p>
     */
    @Size(max = 9, message = "最多上传9张证据图片")
    @Schema(description = "证据图片URL列表", example = "[\"https://xxx/evidence1.jpg\"]")
    private List<String> evidenceImages;
}
