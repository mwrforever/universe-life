package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交成果VO
 */
@Data
@Schema(description = "提交成果信息")
public class SubmitResultVO {

    @Schema(description = "提交内容")
    private String content;

    @Schema(description = "提交图片列表")
    private List<String> images;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;
}
