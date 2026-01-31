package com.universe.life.task.privacy.domain.exception;

/**
 * 任务不存在异常
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class TaskNotFoundException extends TaskBusinessException {

    public TaskNotFoundException(Long taskId) {
        super(TaskErrorCode.TASK_NOT_FOUND, String.format("任务不存在: %d", taskId));
    }

    public TaskNotFoundException() {
        super(TaskErrorCode.TASK_NOT_FOUND);
    }
}
