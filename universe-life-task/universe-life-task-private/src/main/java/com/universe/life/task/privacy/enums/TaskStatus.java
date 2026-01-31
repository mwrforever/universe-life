package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举（基础设施层）
 * <p>
 * 用于数据库持久化的任务状态枚举，与MyBatis-Plus集成。
 * 通过@EnumValue注解标记code字段，实现枚举与数据库整数值的自动转换。
 * </p>
 *
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {
    
    // ==================== 审核阶段 ====================
    
    /**
     * 待审核
     * <p>任务创建后的初始状态，等待平台审核</p>
     */
    PENDING(0, "待审核"),
    
    /**
     * 审核拒绝
     * <p>任务未通过平台审核，需要发布者修改后重新提交</p>
     */
    REJECTED(1, "审核拒绝"),
    
    // ==================== 招募阶段 ====================
    
    /**
     * 招募中
     * <p>任务审核通过，正在招募接单人</p>
     */
    RECRUITING(2, "招募中"),
    
    // ==================== 支付阶段 ====================
    
    /**
     * 待支付
     * <p>有人接单后，等待发布者支付任务押金</p>
     */
    WAIT_PAY(3, "待支付"),
    
    /**
     * 支付中
     * <p>发布者正在进行支付操作</p>
     */
    PAYING(4, "支付中"),
    
    /**
     * 支付失败
     * <p>支付操作失败，需要重新支付</p>
     */
    PAY_FAIL(5, "支付失败"),
    
    // ==================== 执行阶段 ====================
    
    /**
     * 进行中
     * <p>支付成功，任务正在执行中</p>
     */
    PROGRESS(6, "进行中"),
    
    /**
     * 已完成
     * <p>任务执行完成，双方确认结束</p>
     */
    COMPLETED(7, "已完成"),
    
    /**
     * 已取消
     * <p>任务被取消（发布者取消或系统取消）</p>
     */
    CANCELLED(8, "已取消"),
    
    // ==================== 下架状态 ====================
    
    /**
     * 已下架
     * <p>任务被发布者主动下架或被平台下架</p>
     */
    OFFLINE(9, "已下架"),
    
    // ==================== 退款阶段 ====================
    
    /**
     * 待退款
     * <p>任务取消后，等待退款处理</p>
     */
    AWAIT_REFUND(10, "待退款"),
    
    /**
     * 退款中
     * <p>正在进行退款操作</p>
     */
    REFUNDING(11, "退款中"),
    
    /**
     * 退款失败
     * <p>退款操作失败，需要人工处理</p>
     */
    REFUND_FAIL(12, "退款失败");

    /**
     * 状态码
     * <p>用于数据库存储，通过@EnumValue注解实现自动转换</p>
     */
    @EnumValue
    private final Integer code;
    
    /**
     * 状态描述
     * <p>用于前端展示的中文描述</p>
     */
    private final String desc;

    /**
     * 获取状态码（用于JSON序列化）
     *
     * @return 状态码
     */
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    /**
     * 根据状态码获取枚举实例（用于JSON反序列化）
     *
     * @param code 状态码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    @JsonCreator
    public static TaskStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否为待审核状态
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 是否为审核拒绝状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * 是否为招募中状态
     */
    public boolean isRecruiting() {
        return this == RECRUITING;
    }

    /**
     * 是否为进行中状态
     */
    public boolean isInProgress() {
        return this == PROGRESS;
    }

    /**
     * 是否为已完成状态
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 是否可以编辑
     * 只有待审核或审核拒绝状态可以编辑
     */
    public boolean canEdit() {
        return this == PENDING || this == REJECTED;
    }

    /**
     * 是否可以取消
     * 招募中、待支付、支付中状态可以取消
     */
    public boolean canCancel() {
        return this == RECRUITING || this == WAIT_PAY || this == PAYING;
    }

    /**
     * 是否为待支付状态
     */
    public boolean isWaitPay() {
        return this == WAIT_PAY;
    }

    /**
     * 是否为支付中状态
     */
    public boolean isPaying() {
        return this == PAYING;
    }

    /**
     * 是否为支付失败状态
     */
    public boolean isPayFail() {
        return this == PAY_FAIL;
    }

    /**
     * 是否为已取消状态
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * 是否为已下架状态
     */
    public boolean isOffline() {
        return this == OFFLINE;
    }

}
