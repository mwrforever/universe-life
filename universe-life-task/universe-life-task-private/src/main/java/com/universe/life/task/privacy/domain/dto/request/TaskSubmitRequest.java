package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交任务成果请求
 */
@Data
@Schema(description = "提交任务成果请求")
public class TaskSubmitRequest {

    @Schema(description = "提交内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "已完成任务，快递已送达")
    @NotBlank(message = "提交内容不能为空")
    private String content;

    @Schema(description = "成果图片URL列表", example = "[\"https://example.com/img1.jpg\"]")
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}
