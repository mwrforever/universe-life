package com.universe.life.task.privacy.domain.exception;

/**
 * 任务访问权限异常
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class TaskAccessDeniedException extends TaskBusinessException {

    public TaskAccessDeniedException(String message) {
        super(TaskErrorCode.TASK_ACCESS_DENIED, message);
    }

    public TaskAccessDeniedException() {
        super(TaskErrorCode.TASK_ACCESS_DENIED);
    }
}
