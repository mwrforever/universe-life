package com.universe.life.task.privacy.domain.dto;

import com.universe.life.task.privacy.domain.vo.TaskAcceptanceVO;
import com.universe.life.task.privacy.enums.TaskAppealResult;
import com.universe.life.task.privacy.enums.TaskAppealStatus;
import com.universe.life.task.privacy.enums.TaskAppealType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 毛伟然
 * @since 2025/12/11 11:36
 */
@Data
public class TaskAppealDTO {

    private Long id;

    private Long acceptanceId;

    private Long taskId;

    private String taskTitle;

    private Long publisherId;

    private String publisherName;

    private Long appellantId;

    private String appellantName;

    private Long respondentId;

    private String respondentName;

    private TaskAppealType appealType;

    private String reason;

    private String evidenceImagesJson;

    private TaskAppealStatus status;

    private TaskAppealResult result;

    private Long handlerId;

    private String handlerName;

    private String handleRemark;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;

    private TaskAcceptanceVO acceptanceInfo;

}
