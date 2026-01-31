package com.universe.life.task.privacy.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 取消任务命令
 */
@Data
@Builder
public class CancelTaskCommand {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 操作用户ID
     */
    private Long operatorId;

    /**
     * 取消原因
     */
    private String reason;
}
