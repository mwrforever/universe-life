package com.universe.life.task.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 任务接受记录查询参数
 *
 * @author universe-life
 */
@Data
@Schema(description = "任务接受记录查询参数")
public class TaskAcceptanceQuery {

    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "接受者ID")
    private Long acceptorId;

    @Schema(description = "接受状态: 0-进行中, 1-待确认, 2-已完成, 3-已放弃, 4-争议中")
    private Integer status;
}
