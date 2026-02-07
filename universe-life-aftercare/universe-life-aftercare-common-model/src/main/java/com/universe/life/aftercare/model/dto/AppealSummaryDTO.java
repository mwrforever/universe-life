package com.universe.life.aftercare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "申诉摘要DTO")
public class AppealSummaryDTO {

    @Schema(description = "申诉ID")
    private Long appealId;

    @Schema(description = "申诉类型")
    private Integer appealType;

    @Schema(description = "申诉类型描述")
    private String appealTypeText;

    @Schema(description = "申诉状态: 0=待处理, 1=已处理")
    private Integer status;

    @Schema(description = "申诉状态描述")
    private String statusText;

    @Schema(description = "申诉原因")
    private String reason;

    @Schema(description = "申诉时间")
    private LocalDateTime createdAt;
}
