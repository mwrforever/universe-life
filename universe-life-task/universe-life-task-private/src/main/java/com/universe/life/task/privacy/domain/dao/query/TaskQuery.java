package com.universe.life.task.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务查询参数
 *
 * @author universe-life
 */
@Data
@Schema(description = "任务查询参数")
public class TaskQuery {

    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;

    @Schema(description = "任务状态: 0-待审核, 1-待支付, 2-进行中, 3-已完成, 4-已取消, 5-已拒绝, 6-已下架")
    private Integer status;

    @Schema(description = "审核状态: 0-待审核, 1-审核通过, 2-审核拒绝")
    private Integer reviewStatus;

    @Schema(description = "任务分类ID")
    private Long categoryId;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "关键词搜索", maxLength = 50)
    @Size(max = 50, message = "关键词最大长度为50")
    private String keyword;

    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "最小悬赏金额(分)")
    @Min(value = 0, message = "最小悬赏金额不能为负数")
    private Long minReward;

    @Schema(description = "最大悬赏金额(分)")
    @Min(value = 0, message = "最大悬赏金额不能为负数")
    private Long maxReward;
}
