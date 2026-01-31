package com.universe.life.trade.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拒绝信息VO
 * <p>
 * 包含订单被拒绝时的相关信息。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "拒绝信息")
public class RejectInfoVO {

    /** 拒绝原因 */
    @Schema(description = "拒绝原因")
    private String reason;

    /** 拒绝时间 */
    @Schema(description = "拒绝时间")
    private LocalDateTime rejectedAt;
    
    /** 拒绝人ID */
    @Schema(description = "拒绝人ID")
    private Long rejectedBy;
}
