package com.universe.life.task.privacy.domain.exception;

/**
 * 任务状态无效异常
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class TaskInvalidStatusException extends TaskBusinessException {

    public TaskInvalidStatusException(String message) {
        super(TaskErrorCode.TASK_INVALID_STATUS, message);
    }

    public TaskInvalidStatusException() {
        super(TaskErrorCode.TASK_INVALID_STATUS);
    }
}
