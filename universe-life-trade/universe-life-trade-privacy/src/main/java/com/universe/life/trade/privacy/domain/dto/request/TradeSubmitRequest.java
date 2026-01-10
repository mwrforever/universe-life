package com.universe.life.trade.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交任务成果请求
 *
 * @author universe-life
 */
@Data
@Schema(description = "提交任务成果请求")
public class TradeSubmitRequest {

    @Schema(description = "提交内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "提交内容不能为空")
    private String content;

    @Schema(description = "提交图片URL列表")
    @Size(max = 9, message = "图片数量不能超过9张")
    private List<String> images;
}
