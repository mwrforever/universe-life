package com.universe.life.aftercare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建申诉DTO（服务间调用）")
public class CreateAppealDTO {

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bizId;

    @Schema(description = "业务类型：1订单 2任务", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer bizType;

    @Schema(description = "申诉人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long appellantId;

    @Schema(description = "申诉类型: 1-成果不符合要求, 2-发布者恶意拒绝, 3-其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer appealType;

    @Schema(description = "申诉原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "证据图片URL列表")
    private List<String> evidenceImages;
}
