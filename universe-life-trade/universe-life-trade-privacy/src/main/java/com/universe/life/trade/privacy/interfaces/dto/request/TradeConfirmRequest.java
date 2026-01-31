package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 确认验收请求
 * <p>
 * 发布者确认验收成果时使用，可选择性地对接单者进行评分和评价。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "确认验收请求")
public class TradeConfirmRequest {

    /**
     * 评分
     * <p>
     * 可选，1-5分。
     * 对接单者完成任务的质量进行评分。
     * </p>
     */
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    @Schema(description = "评分（1-5分）", example = "5")
    private Integer rating;

    /**
     * 评价内容
     * <p>
     * 可选，对接单者的文字评价。
     * </p>
     */
    @Schema(description = "评价内容", example = "完成得很好，非常满意")
    private String comment;
}
