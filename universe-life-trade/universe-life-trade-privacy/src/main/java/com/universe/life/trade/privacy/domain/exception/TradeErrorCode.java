package com.universe.life.trade.privacy.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易服务错误码枚举
 * 错误码范围：6000-6999
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Getter
@AllArgsConstructor
public enum TradeErrorCode {

    // 订单相关错误码 6000-6099
    ORDER_NOT_FOUND(6000, "订单不存在"),
    ORDER_ACCESS_DENIED(6001, "无权访问该订单"),
    ORDER_INVALID_STATUS(6002, "订单状态不允许此操作"),
    ORDER_INVALID_STATUS_TRANSITION(6003, "非法的订单状态转换"),
    ORDER_ALREADY_EXISTS(6004, "已存在该任务的订单"),
    ORDER_TASK_NOT_FOUND(6005, "关联的任务不存在"),
    ORDER_TASK_INVALID_STATUS(6006, "任务状态不允许接单"),
    ORDER_ACCEPTOR_LIMIT_REACHED(6007, "任务接单人数已达上限"),
    ORDER_DEADLINE_PASSED(6008, "任务已过截止时间"),
    ORDER_SELF_ACCEPT_NOT_ALLOWED(6009, "不能接自己发布的任务"),
    ORDER_CANNOT_APPROVE(6010, "订单无法审批通过"),
    ORDER_CANNOT_REJECT(6011, "订单无法拒绝"),
    ORDER_CANNOT_SUBMIT(6012, "订单无法提交成果"),
    ORDER_CANNOT_CONFIRM(6013, "订单无法确认验收"),
    ORDER_CANNOT_ABANDON(6014, "订单无法放弃"),
    ORDER_SUBMIT_CONTENT_REQUIRED(6015, "提交内容不能为空"),
    ORDER_REJECT_REASON_REQUIRED(6016, "拒绝原因不能为空"),
    
    // 申诉相关错误码 6100-6199
    APPEAL_NOT_FOUND(6100, "申诉记录不存在"),
    APPEAL_ACCESS_DENIED(6101, "无权访问该申诉"),
    APPEAL_ALREADY_EXISTS(6102, "已存在待处理的申诉"),
    APPEAL_ALREADY_PROCESSED(6103, "申诉已处理"),
    APPEAL_INVALID_STATUS(6104, "申诉状态不允许此操作"),
    APPEAL_ORDER_INVALID_STATUS(6105, "订单状态不允许发起申诉"),
    APPEAL_REASON_REQUIRED(6106, "申诉原因不能为空"),
    APPEAL_TYPE_REQUIRED(6107, "申诉类型不能为空"),
    
    // 任务相关错误码 6200-6299（交易服务调用任务服务时使用）
    TASK_NOT_FOUND(6200, "关联的任务不存在"),
    TASK_NOT_RECRUITING(6201, "任务未在招募中"),
    TASK_ACCEPTOR_LIMIT_REACHED(6202, "任务接单人数已达上限"),
    TASK_DEADLINE_PASSED(6203, "任务已过截止时间"),
    TASK_SELF_ACCEPT_NOT_ALLOWED(6204, "不能接自己发布的任务"),
    
    // 用户相关错误码 6300-6399
    USER_NOT_FOUND(6300, "用户不存在"),
    PUBLISHER_NOT_FOUND(6301, "发布者不存在"),
    ACCEPTOR_NOT_FOUND(6302, "接单者不存在"),
    
    // 业务规则错误码 6400-6499
    RESULT_CONTENT_TOO_SHORT(6400, "成果描述长度不足"),
    RESULT_CONTENT_TOO_LONG(6401, "成果描述长度超限"),
    RESULT_IMAGES_TOO_MANY(6402, "成果图片数量超限"),
    APPEAL_REASON_TOO_SHORT(6403, "申诉原因长度不足"),
    APPEAL_REASON_TOO_LONG(6404, "申诉原因长度超限"),
    APPEAL_EVIDENCE_TOO_MANY(6405, "申诉证据图片数量超限");

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
    public static TradeErrorCode of(int code) {
        for (TradeErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
