package com.universe.life.task.privacy.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务服务错误码枚举
 * 错误码范围：5000-5999
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TaskErrorCode {

    // 任务相关错误码 5000-5099
    TASK_NOT_FOUND(5000, "任务不存在"),
    TASK_ACCESS_DENIED(5001, "无权访问该任务"),
    TASK_INVALID_STATUS(5002, "任务状态不允许此操作"),
    TASK_INVALID_STATUS_TRANSITION(5003, "非法的任务状态转换"),
    TASK_ALREADY_APPLIED(5004, "已申请过该任务"),
    TASK_ACCEPTOR_LIMIT_REACHED(5005, "接单人数已达上限"),
    TASK_DEADLINE_PASSED(5006, "任务已过截止时间"),
    TASK_CATEGORY_NOT_FOUND(5007, "任务分类不存在"),
    TASK_CATEGORY_NAME_EXISTS(5008, "分类名称已存在"),
    TASK_CATEGORY_CODE_EXISTS(5009, "分类代码已存在"),
    TASK_CANNOT_CANCEL(5010, "任务无法取消"),
    TASK_CANNOT_UPDATE(5011, "任务无法编辑"),
    TASK_DEPOSIT_NOT_PAID(5012, "任务保证金未支付"),
    TASK_REVIEW_PENDING(5013, "任务审核中"),
    TASK_REVIEW_REJECTED(5014, "任务审核未通过"),

    // 任务接单相关错误码 5100-5199
    TASK_ACCEPTANCE_NOT_FOUND(5100, "接单记录不存在"),
    TASK_ACCEPTANCE_ACCESS_DENIED(5101, "无权访问该接单记录"),
    TASK_ACCEPTANCE_INVALID_STATUS(5102, "接单状态不允许此操作"),
    TASK_ACCEPTANCE_ALREADY_EXISTS(5103, "已存在该任务的接单申请"),
    TASK_SELF_ACCEPT_NOT_ALLOWED(5104, "不能接自己发布的任务"),

    // 任务审核相关错误码 5200-5299
    TASK_REVIEW_NOT_FOUND(5200, "审核记录不存在"),
    TASK_REVIEW_ALREADY_PROCESSED(5201, "审核已处理"),

    // 任务申诉相关错误码 5300-5399
    TASK_APPEAL_NOT_FOUND(5300, "申诉记录不存在"),
    TASK_APPEAL_ALREADY_EXISTS(5301, "已存在待处理的申诉"),
    TASK_APPEAL_ALREADY_PROCESSED(5302, "申诉已处理"),
    TASK_APPEAL_ACCESS_DENIED(5303, "无权发起申诉");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误描述
     */
    private final String message;

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 错误码枚举，未找到返回null
     */
    public static TaskErrorCode of(int code) {
        for (TaskErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
