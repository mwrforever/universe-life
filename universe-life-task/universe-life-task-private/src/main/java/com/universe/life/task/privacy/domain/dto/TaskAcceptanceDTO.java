package com.universe.life.task.privacy.domain.dto;

import com.universe.life.task.privacy.enums.TaskAcceptanceStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务接受记录DTO
 *
 * @author universe-life
 */
@Data
public class TaskAcceptanceDTO {

    private Long id;

    private Long taskId;

    private String taskTitle;

    private Long publisherId;

    private String publisherName;

    private Long acceptorId;

    private String acceptorName;

    private Long rewardAmount;

    private TaskAcceptanceStatus status;

    private String submitContent;

    private String submitImagesJson;

    private LocalDateTime acceptedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    private LocalDateTime deadline;

    private String rejectReason;
}
