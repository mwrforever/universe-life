package com.universe.life.trade.privacy.domain.exception;

import com.universe.life.auth.common.exception.BusinessException;
import lombok.Getter;

/**
 * 交易服务业务异常
 *
 * @author universe-life
 */
@Getter
public class TradeBusinessException extends BusinessException {

    /**
     * 交易错误码
     */
    private final TradeErrorCode errorCode;

    public TradeBusinessException(TradeErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TradeBusinessException(TradeErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }

    public TradeBusinessException(TradeErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public TradeBusinessException(TradeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
        this.errorCode = errorCode;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 订单不存在
     */
    public static TradeBusinessException orderNotFound() {
        return new TradeBusinessException(TradeErrorCode.ORDER_NOT_FOUND);
    }

    public static TradeBusinessException orderNotFound(Long orderId) {
        return new TradeBusinessException(TradeErrorCode.ORDER_NOT_FOUND,
                String.format("订单不存在: %d", orderId));
    }

    /**
     * 无权访问订单
     */
    public static TradeBusinessException orderAccessDenied() {
        return new TradeBusinessException(TradeErrorCode.ORDER_ACCESS_DENIED);
    }

    /**
     * 订单状态不允许操作
     */
    public static TradeBusinessException orderInvalidStatus(String operation) {
        return new TradeBusinessException(TradeErrorCode.ORDER_INVALID_STATUS,
                String.format("当前订单状态不允许%s操作", operation));
    }

    /**
     * 非法的状态转换
     */
    public static TradeBusinessException orderInvalidStatusTransition(String from, String to) {
        return new TradeBusinessException(TradeErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                String.format("订单状态不能从[%s]转换到[%s]", from, to));
    }

    /**
     * 已存在该任务的订单
     */
    public static TradeBusinessException orderAlreadyExists() {
        return new TradeBusinessException(TradeErrorCode.ORDER_ALREADY_EXISTS);
    }

    public static TradeBusinessException orderAlreadyExists(Long taskId) {
        return new TradeBusinessException(TradeErrorCode.ORDER_ALREADY_EXISTS,
                String.format("已存在任务[%d]的订单", taskId));
    }

    /**
     * 订单无法批准
     */
    public static TradeBusinessException orderCannotApprove() {
        return new TradeBusinessException(TradeErrorCode.ORDER_CANNOT_APPROVE);
    }

    /**
     * 订单无法拒绝
     */
    public static TradeBusinessException orderCannotReject() {
        return new TradeBusinessException(TradeErrorCode.ORDER_CANNOT_REJECT);
    }

    /**
     * 订单无法提交成果
     */
    public static TradeBusinessException orderCannotSubmit() {
        return new TradeBusinessException(TradeErrorCode.ORDER_CANNOT_SUBMIT);
    }

    /**
     * 订单无法确认验收
     */
    public static TradeBusinessException orderCannotConfirm() {
        return new TradeBusinessException(TradeErrorCode.ORDER_CANNOT_CONFIRM);
    }

    /**
     * 订单无法放弃
     */
    public static TradeBusinessException orderCannotAbandon() {
        return new TradeBusinessException(TradeErrorCode.ORDER_CANNOT_ABANDON);
    }

    /**
     * 提交内容不能为空
     */
    public static TradeBusinessException submitContentRequired() {
        return new TradeBusinessException(TradeErrorCode.ORDER_SUBMIT_CONTENT_REQUIRED);
    }

    /**
     * 拒绝原因不能为空
     */
    public static TradeBusinessException rejectReasonRequired() {
        return new TradeBusinessException(TradeErrorCode.ORDER_REJECT_REASON_REQUIRED);
    }

    /**
     * 申诉已存在
     */
    public static TradeBusinessException appealAlreadyExists() {
        return new TradeBusinessException(TradeErrorCode.APPEAL_ALREADY_EXISTS);
    }

    /**
     * 申诉不存在
     */
    public static TradeBusinessException appealNotFound() {
        return new TradeBusinessException(TradeErrorCode.APPEAL_NOT_FOUND);
    }

    /**
     * 无权发起申诉
     */
    public static TradeBusinessException appealAccessDenied() {
        return new TradeBusinessException(TradeErrorCode.APPEAL_ACCESS_DENIED);
    }

    /**
     * 申诉原因不能为空
     */
    public static TradeBusinessException appealReasonRequired() {
        return new TradeBusinessException(TradeErrorCode.APPEAL_REASON_REQUIRED);
    }

    /**
     * 申诉类型不能为空
     */
    public static TradeBusinessException appealTypeRequired() {
        return new TradeBusinessException(TradeErrorCode.APPEAL_TYPE_REQUIRED);
    }

    /**
     * 关联任务不存在
     */
    public static TradeBusinessException taskNotFound() {
        return new TradeBusinessException(TradeErrorCode.TASK_NOT_FOUND);
    }

    public static TradeBusinessException taskNotFound(Long taskId) {
        return new TradeBusinessException(TradeErrorCode.TASK_NOT_FOUND,
                String.format("关联任务不存在: %d", taskId));
    }

    /**
     * 任务未在招募中
     */
    public static TradeBusinessException taskNotRecruiting() {
        return new TradeBusinessException(TradeErrorCode.TASK_NOT_RECRUITING);
    }

    /**
     * 任务接单人数已达上限
     */
    public static TradeBusinessException taskAcceptorLimitReached() {
        return new TradeBusinessException(TradeErrorCode.TASK_ACCEPTOR_LIMIT_REACHED);
    }

    /**
     * 任务已过截止时间
     */
    public static TradeBusinessException taskDeadlinePassed() {
        return new TradeBusinessException(TradeErrorCode.TASK_DEADLINE_PASSED);
    }

    /**
     * 不能接自己发布的任务
     */
    public static TradeBusinessException taskSelfAcceptNotAllowed() {
        return new TradeBusinessException(TradeErrorCode.TASK_SELF_ACCEPT_NOT_ALLOWED);
    }

    /**
     * 用户不存在
     */
    public static TradeBusinessException userNotFound() {
        return new TradeBusinessException(TradeErrorCode.USER_NOT_FOUND);
    }

    /**
     * 发布者不存在
     */
    public static TradeBusinessException publisherNotFound() {
        return new TradeBusinessException(TradeErrorCode.PUBLISHER_NOT_FOUND);
    }

    /**
     * 接单者不存在
     */
    public static TradeBusinessException acceptorNotFound() {
        return new TradeBusinessException(TradeErrorCode.ACCEPTOR_NOT_FOUND);
    }
}
