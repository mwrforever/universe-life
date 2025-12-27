package com.universe.life.task.privacy.domain.vo;

import com.universe.life.task.privacy.enums.TaskAcceptanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务接受记录VO
 */
@Data
@Schema(description = "任务接受记录信息")
public class TaskAcceptanceVO {

    @Schema(description = "接受记录ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务标题")
    private String taskTitle;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "接受者ID")
    private Long acceptorId;

    @Schema(description = "接受者名称")
    private String acceptorName;

    @Schema(description = "悬赏金额(分)")
    private Long rewardAmount;

    @Schema(description = "接受状态")
    private TaskAcceptanceStatus status;

    @Schema(description = "提交内容")
    private String submitContent;

    @Schema(description = "提交图片列表")
    private List<String> submitImages;

    @Schema(description = "提交图片JSON", hidden = true)
    private String submitImagesJson;

    @Schema(description = "接受时间")
    private LocalDateTime acceptedAt;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "拒绝原因")
    private String rejectReason;
}
