package com.universe.life.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户端支付记录分页查询参数")
public class UserPayRecordListQueryDTO {

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "渠道")
    private Integer channel;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建开始时间")
    private LocalDateTime startTime;

    @Schema(description = "创建结束时间")
    private LocalDateTime endTime;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "页大小", defaultValue = "20")
    private Integer size = 20;
}
