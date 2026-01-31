package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交成果请求
 */
@Data
@Schema(description = "提交成果请求")
public class TradeSubmitRequest {

    @NotBlank(message = "成果描述不能为空")
    @Size(min = 10, max = 2000, message = "成果描述长度为10-2000个字符")
    @Schema(description = "成果描述", example = "已完成Logo设计，请查收附件", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Size(max = 9, message = "最多上传9张图片")
    @Schema(description = "成果图片URL列表", example = "[\"https://xxx/result1.jpg\", \"https://xxx/result2.jpg\"]")
    private List<String> images;
}
