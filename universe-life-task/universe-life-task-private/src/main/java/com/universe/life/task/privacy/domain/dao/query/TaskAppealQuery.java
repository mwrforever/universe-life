package com.universe.life.task.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务申诉查询参数
 *
 * @author universe-life
 */
@Data
@Schema(description = "任务申诉查询参数")
public class TaskAppealQuery {

    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;

    @Schema(description = "申诉状态: 0-待处理, 1-已处理")
    private Integer status;

    @Schema(description = "申诉类型: 0-接受者申诉, 1-发布者申诉")
    private Integer appealType;

    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;
}
