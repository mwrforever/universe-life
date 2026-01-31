package com.universe.life.trade.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 交易订单查询请求
 * <p>
 * 用于分页查询交易订单列表，支持按状态筛选。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "交易订单查询请求")
public class TradeOrderQueryRequest {

    /**
     * 订单状态筛选
     * <p>
     * 可选值：
     * <ul>
     *   <li>0 - 待审批</li>
     *   <li>1 - 已拒绝</li>
     *   <li>2 - 进行中</li>
     *   <li>3 - 待确认</li>
     *   <li>4 - 待付款</li>
     *   <li>5 - 已完成</li>
     *   <li>6 - 已放弃</li>
     *   <li>7 - 申诉中</li>
     * </ul>
     * 不传则查询所有状态。
     * </p>
     */
    @Schema(description = "订单状态: 0-待审批, 1-已拒绝, 2-进行中, 3-待确认, 4-待付款, 5-已完成, 6-已放弃, 7-申诉中", example = "2")
    private Integer status;

    /**
     * 页码
     * <p>从1开始，默认为1</p>
     */
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，默认1", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     * <p>默认20，最大100</p>
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    @Schema(description = "每页数量，默认20，最大100", example = "20", defaultValue = "20")
    private Integer pageSize = 20;
}
