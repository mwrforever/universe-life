package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理申诉请求
 * <p>
 * 管理员处理申诉时使用的请求参数。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "处理申诉请求")
public class HandleAppealRequest {

    /**
     * 处理结果
     * <p>
     * 1=支持申诉方, 2=驳回
     * </p>
     */
    @NotNull(message = "处理结果不能为空")
    @Schema(description = "处理结果: 1=支持申诉方, 2=驳回", required = true, example = "1")
    private Integer result;

    /**
     * 处理备注
     * <p>
     * 管理员对处理结果的说明，可选。
     * </p>
     */
    @Schema(description = "处理备注", example = "经核实，接单方提交的成果符合要求")
    private String handleRemark;
}
