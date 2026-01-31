package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易订单状态枚举（基础设施层）
 * <p>
 * 用于数据库持久化的订单状态枚举，与MyBatis-Plus集成。
 * 通过@EnumValue注解标记code字段，实现枚举与数据库整数值的自动转换。
 * </p>
 * 
 * <p>订单状态流转说明：</p>
 * <ul>
 *   <li>审批流程：PENDING → REJECTED / PROGRESS</li>
 *   <li>执行流程：PROGRESS → SUBMIT → PAYMENT → COMPLETED</li>
 *   <li>放弃流程：PROGRESS → ABANDONED</li>
 *   <li>申诉流程：SUBMIT → DISPUTE → PAYMENT / PROGRESS / COMPLETED</li>
 *   <li>评价流程：PAYMENT → RATE → COMPLETED</li>
 * </ul>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeOrderStatus {
    
    // ==================== 审批阶段 ====================
    
    /**
     * 待审批
     * <p>接单申请已提交，等待发布者审批</p>
     */
    PENDING(0, "待审批"),
    
    // ==================== 执行阶段 ====================
    
    /**
     * 进行中
     * <p>发布者已同意接单，任务正在执行中</p>
     */
    PROGRESS(1, "进行中"),
    
    /**
     * 待确认
     * <p>接单者已提交成果，等待发布者确认验收</p>
     */
    SUBMIT(2, "待确认"),
    
    // ==================== 结算阶段 ====================
    
    /**
     * 待收款
     * <p>发布者已确认验收，等待系统结算付款</p>
     */
    PAYMENT(3, "待收款"),
    
    // ==================== 争议阶段 ====================
    
    /**
     * 争议中
     * <p>订单存在争议，等待平台介入处理</p>
     */
    DISPUTE(4, "争议中"),
    
    // ==================== 付款阶段 ====================
    
    /**
     * 付款中
     * <p>系统正在处理付款</p>
     */
    PAYING(5, "付款中"),
    
    // ==================== 终态 ====================
    
    /**
     * 已完成
     * <p>订单已完成，任务结束</p>
     */
    COMPLETED(6, "已完成"),
    
    /**
     * 已拒绝
     * <p>发布者拒绝了接单申请</p>
     */
    REJECTED(7, "已拒绝"),
    
    /**
     * 已放弃
     * <p>接单者主动放弃了任务</p>
     */
    ABANDONED(8, "已放弃"),
    
    // ==================== 评价阶段 ====================
    
    /**
     * 待评价
     * <p>任务已完成，等待双方评价</p>
     */
    RATE(9, "待评价");

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
    public static TradeOrderStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeOrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
