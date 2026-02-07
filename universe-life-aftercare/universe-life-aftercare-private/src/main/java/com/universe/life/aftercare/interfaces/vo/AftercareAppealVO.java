package com.universe.life.aftercare.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "申诉信息")
public class AftercareAppealVO {

    @Schema(description = "申诉ID")
    private Long appealId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "申诉人ID")
    private Long appellantId;

    @Schema(description = "申诉类型")
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

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
