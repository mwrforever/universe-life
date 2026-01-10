package com.universe.life.task.model.dto;

import com.universe.life.task.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务信息DTO（供其他服务调用）
 *
 * @author universe-life
 */
@Data
public class TaskInfoDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 悬赏金额（分）
     */
    private Long rewardAmount;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 最大接受人数
     */
    private Integer maxAcceptors;

    /**
     * 当前接受人数
     */
    private Integer currentAcceptors;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;
}
