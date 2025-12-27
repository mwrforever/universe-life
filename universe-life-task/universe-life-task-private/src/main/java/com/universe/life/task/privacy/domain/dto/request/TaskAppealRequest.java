package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发起申诉请求
 */
@Data
@Schema(description = "发起申诉请求")
public class TaskAppealRequest {

    @Schema(description = "申诉原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "任务发布者未按约定支付报酬")
    @NotBlank(message = "申诉原因不能为空")
    private String reason;

    @Schema(description = "证据图片URL列表", example = "[\"https://example.com/img1.jpg\"]")
    @Size(max = 9, message = "最多上传9张证据图片")
    private List<String> evidenceImages;
}
