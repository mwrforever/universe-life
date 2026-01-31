package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 任务大厅查询请求
 * <p>
 * 用于查询任务大厅列表，支持：
 * <ul>
 *   <li>按分类筛选</li>
 *   <li>按悬赏金额范围筛选</li>
 *   <li>按指定字段排序</li>
 *   <li>分页查询</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "任务大厅查询请求")
public class TaskHallQueryRequest {

    /** 分类ID，用于筛选特定分类的任务 */
    @Schema(description = "分类ID", example = "4")
    private Long categoryId;

    /** 最低悬赏金额（分），用于筛选悬赏金额大于等于该值的任务 */
    @Min(value = 0, message = "最低悬赏金额不能为负数")
    @Schema(description = "最低悬赏金额（分）", example = "10000")
    private Long minReward;

    /** 最高悬赏金额（分），用于筛选悬赏金额小于等于该值的任务 */
    @Min(value = 0, message = "最高悬赏金额不能为负数")
    @Schema(description = "最高悬赏金额（分）", example = "100000")
    private Long maxReward;

    /** 排序字段：reward-悬赏金额, deadline-截止时间, created-创建时间 */
    @Schema(description = "排序字段: reward-悬赏金额, deadline-截止时间, created-创建时间", example = "created", allowableValues = {"reward", "deadline", "created"})
    private String sortBy;

    /** 排序方向：asc-升序, desc-降序 */
    @Schema(description = "排序方向: asc-升序, desc-降序", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    /** 页码，从1开始，默认为1 */
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，默认1", example = "1")
    private Integer pageNum = 1;

    /** 每页数量，默认20，最大100 */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    @Schema(description = "每页数量，默认20，最大100", example = "20")
    private Integer pageSize = 20;
}
