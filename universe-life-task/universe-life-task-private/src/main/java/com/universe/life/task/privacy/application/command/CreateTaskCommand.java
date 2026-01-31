package com.universe.life.task.privacy.application.command;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建任务命令
 */
@Data
@Builder
public class CreateTaskCommand {

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 悬赏金额（分）
     */
    private Long rewardAmount;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 最大接单人数
     */
    private Integer maxAcceptors;
}
