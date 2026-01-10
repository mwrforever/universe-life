package com.universe.life.trade.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 交易订单查询参数
 *
 * @author universe-life
 */
@Data
@Schema(description = "交易订单查询参数")
public class TradeOrderQuery {

    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;

    @Schema(description = "需求ID")
    private Long taskId;

    @Schema(description = "订单状态: 0-待审批, 1-进行中, 2-待确认, 3-待收款, 4-争议中, 5-已完成, 6-已拒绝, 7-已放弃, 8-待评价")
    private Integer status;
}
