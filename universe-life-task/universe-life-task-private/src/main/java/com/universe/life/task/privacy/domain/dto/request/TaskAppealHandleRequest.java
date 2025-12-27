package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理申诉请求
 */
@Data
@Schema(description = "处理申诉请求")
public class TaskAppealHandleRequest {

    @Schema(description = "处理结果(1-支持申诉方,2-支持被申诉方,3-双方各打五十大板)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "处理结果不能为空")
    private Integer result;

    @Schema(description = "处理备注", example = "经核实，申诉方提供的证据充分")
    private String remark;
}
