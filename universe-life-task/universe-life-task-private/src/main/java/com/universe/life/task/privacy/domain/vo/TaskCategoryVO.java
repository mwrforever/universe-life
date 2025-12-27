package com.universe.life.task.privacy.domain.vo;

import com.universe.life.task.privacy.enums.CommonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务分类VO
 */
@Data
@Schema(description = "任务分类信息")
public class TaskCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态")
    private CommonStatus status;
}
