package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单时间线VO
 */
@Data
@Schema(description = "订单时间线")
public class OrderTimelineVO {

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作时间")
    private LocalDateTime time;

    @Schema(description = "操作描述")
    private String description;
}
