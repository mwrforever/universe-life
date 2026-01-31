package com.universe.life.task.privacy.domain.exception;

import com.universe.life.auth.common.exception.BusinessException;
import lombok.Getter;

/**
 * 任务服务业务异常
 *
 * @author universe-life
 */
@Getter
public class TaskBusinessException extends BusinessException {

    /**
     * 任务错误码
     */
    private final TaskErrorCode errorCode;

    public TaskBusinessException(TaskErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TaskBusinessException(TaskErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }

    public TaskBusinessException(TaskErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public TaskBusinessException(TaskErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
        this.errorCode = errorCode;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 任务不存在
     */
    public static TaskBusinessException taskNotFound() {
        return new TaskBusinessException(TaskErrorCode.TASK_NOT_FOUND);
    }

    public static TaskBusinessException taskNotFound(Long taskId) {
        return new TaskBusinessException(TaskErrorCode.TASK_NOT_FOUND, 
                String.format("任务不存在: %d", taskId));
    }

    /**
     * 无权访问任务
     */
    public static TaskBusinessException taskAccessDenied() {
        return new TaskBusinessException(TaskErrorCode.TASK_ACCESS_DENIED);
    }

    /**
     * 任务状态不允许操作
     */
    public static TaskBusinessException taskInvalidStatus(String operation) {
        return new TaskBusinessException(TaskErrorCode.TASK_INVALID_STATUS,
                String.format("当前任务状态不允许%s操作", operation));
    }

    /**
     * 非法的状态转换
     */
    public static TaskBusinessException taskInvalidStatusTransition(String from, String to) {
        return new TaskBusinessException(TaskErrorCode.TASK_INVALID_STATUS_TRANSITION,
                String.format("任务状态不能从[%s]转换到[%s]", from, to));
    }

    /**
     * 已申请过该任务
     */
    public static TaskBusinessException taskAlreadyApplied() {
        return new TaskBusinessException(TaskErrorCode.TASK_ALREADY_APPLIED);
    }

    /**
     * 接单人数已达上限
     */
    public static TaskBusinessException taskAcceptorLimitReached() {
        return new TaskBusinessException(TaskErrorCode.TASK_ACCEPTOR_LIMIT_REACHED);
    }

    /**
     * 任务已过截止时间
     */
    public static TaskBusinessException taskDeadlinePassed() {
        return new TaskBusinessException(TaskErrorCode.TASK_DEADLINE_PASSED);
    }

    /**
     * 任务分类不存在
     */
    public static TaskBusinessException taskCategoryNotFound() {
        return new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NOT_FOUND);
    }

    public static TaskBusinessException taskCategoryNotFound(Long categoryId) {
        return new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NOT_FOUND,
                String.format("任务分类不存在: %d", categoryId));
    }

    /**
     * 任务无法取消
     */
    public static TaskBusinessException taskCannotCancel() {
        return new TaskBusinessException(TaskErrorCode.TASK_CANNOT_CANCEL);
    }

    /**
     * 任务无法编辑
     */
    public static TaskBusinessException taskCannotUpdate() {
        return new TaskBusinessException(TaskErrorCode.TASK_CANNOT_UPDATE);
    }

    /**
     * 不能接自己发布的任务
     */
    public static TaskBusinessException taskSelfAcceptNotAllowed() {
        return new TaskBusinessException(TaskErrorCode.TASK_SELF_ACCEPT_NOT_ALLOWED);
    }

    /**
     * 接单记录不存在
     */
    public static TaskBusinessException acceptanceNotFound() {
        return new TaskBusinessException(TaskErrorCode.TASK_ACCEPTANCE_NOT_FOUND);
    }

    /**
     * 已存在该任务的接单申请
     */
    public static TaskBusinessException acceptanceAlreadyExists() {
        return new TaskBusinessException(TaskErrorCode.TASK_ACCEPTANCE_ALREADY_EXISTS);
    }

    /**
     * 申诉已存在
     */
    public static TaskBusinessException appealAlreadyExists() {
        return new TaskBusinessException(TaskErrorCode.TASK_APPEAL_ALREADY_EXISTS);
    }

    /**
     * 申诉不存在
     */
    public static TaskBusinessException appealNotFound() {
        return new TaskBusinessException(TaskErrorCode.TASK_APPEAL_NOT_FOUND);
    }
}
