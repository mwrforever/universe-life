package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易申诉VO
 * <p>
 * 用于返回申诉的完整信息，包括申诉详情、处理结果等。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "交易申诉信息")
public class TradeAppealVO {

    @Schema(description = "申诉ID")
    private Long appealId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "申诉人ID")
    private Long appellantId;

    @Schema(description = "申诉类型: 1=接单方申诉, 2=发布方申诉")
    private Integer appealType;

    @Schema(description = "申诉类型描述")
    private String appealTypeText;

    @Schema(description = "申诉原因")
    private String reason;

    @Schema(description = "证据图片URL列表")
    private List<String> evidenceImages;

    @Schema(description = "申诉状态: 0=待处理, 1=已处理")
    private Integer status;

    @Schema(description = "申诉状态描述")
    private String statusText;

    @Schema(description = "处理结果: 1=支持申诉方, 2=驳回")
    private Integer result;

    @Schema(description = "处理结果描述")
    private String resultText;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
