package com.universe.life.task.privacy.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 我发布的任务查询请求
 * <p>
 * 用于查询当前用户发布的任务列表，支持：
 * <ul>
 *   <li>按状态筛选</li>
 *   <li>分页查询</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Data
@Schema(description = "我发布的任务查询请求")
public class TaskPublishedQueryRequest {

    /** 任务状态筛选条件，不传则查询所有状态 */
    @Schema(description = "任务状态: 0-待审核, 1-审核拒绝, 2-招募中, 3-进行中, 4-已完成, 5-已取消", example = "2")
    private Integer status;

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
