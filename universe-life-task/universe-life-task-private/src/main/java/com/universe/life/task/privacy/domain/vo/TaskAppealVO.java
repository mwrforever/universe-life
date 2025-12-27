package com.universe.life.task.privacy.domain.vo;

import com.universe.life.task.privacy.enums.TaskAppealResult;
import com.universe.life.task.privacy.enums.TaskAppealStatus;
import com.universe.life.task.privacy.enums.TaskAppealType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务申诉VO
 */
@Data
@Schema(description = "任务申诉信息")
public class TaskAppealVO {

    @Schema(description = "申诉ID")
    private Long id;

    @Schema(description = "接受记录ID")
    private Long acceptanceId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String taskTitle;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "申诉人ID")
    private Long appellantId;

    @Schema(description = "申诉人名称")
    private String appellantName;

    @Schema(description = "被申诉人ID")
    private Long respondentId;

    @Schema(description = "被申诉人名称")
    private String respondentName;

    @Schema(description = "申诉类型")
    private TaskAppealType appealType;

    @Schema(description = "申诉原因")
    private String reason;

    @Schema(description = "证据图片列表")
    private List<String> evidenceImages;

    @Schema(description = "申诉状态")
    private TaskAppealStatus status;

    @Schema(description = "处理结果")
    private TaskAppealResult result;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人名称")
    private String handlerName;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "处理时间")
    private LocalDateTime handledAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "接受记录信息")
    private TaskAcceptanceVO acceptanceInfo;
}
