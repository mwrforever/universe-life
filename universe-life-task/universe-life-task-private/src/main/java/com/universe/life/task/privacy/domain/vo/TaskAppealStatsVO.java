package com.universe.life.task.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 申诉统计VO
 */
@Data
@Schema(description = "申诉统计信息")
public class TaskAppealStatsVO {

    @Schema(description = "总申诉数")
    private Integer total;

    @Schema(description = "待处理数")
    private Integer pending;

    @Schema(description = "已处理数")
    private Integer handled;

    @Schema(description = "接受者申诉数")
    private Integer acceptorAppeal;

    @Schema(description = "发布者申诉数")
    private Integer publisherAppeal;
}
