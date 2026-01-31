package com.universe.life.task.privacy.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 任务详情查询
 */
@Data
@Builder
public class TaskDetailQuery {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 当前用户ID
     */
    private Long currentUserId;
}
