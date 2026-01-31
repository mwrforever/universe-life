package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.Getter;

import java.util.*;

/**
 * 交易订单状态枚举（领域层）
 * <p>
 * 领域层的订单状态枚举，包含完整的状态转换逻辑和业务规则。
 * 与基础设施层的TradeOrderStatus不同，此枚举专注于领域业务逻辑。
 * </p>
 * 
 * <p>状态转换规则：</p>
 * <ul>
 *   <li>PENDING → PROGRESS（同意接单）、REJECTED（拒绝接单）</li>
 *   <li>PROGRESS → SUBMIT（提交成果）、ABANDONED（放弃任务）</li>
 *   <li>SUBMIT → PAYMENT（确认验收）、DISPUTE（发起申诉）、PROGRESS（打回修改）</li>
 *   <li>PAYMENT → COMPLETED（收款完成）、RATE（进入评价）</li>
 *   <li>DISPUTE → COMPLETED（申诉处理完成）、PAYMENT（申诉通过）、PROGRESS（申诉驳回）</li>
 *   <li>RATE → COMPLETED（评价完成）</li>
 * </ul>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
public enum TradeOrderStatusEnum {

    // ==================== 审批阶段 ====================
    
    /**
     * 待审批
     * <p>接单申请已提交，等待发布者审批</p>
     */
    PENDING(0, "待审批"),
    
    /**
     * 已拒绝
     * <p>发布者拒绝了接单申请（终态）</p>
     */
    REJECTED(1, "已拒绝"),
    
    // ==================== 执行阶段 ====================
    
    /**
     * 进行中
     * <p>发布者已同意接单，任务正在执行中</p>
     */
    PROGRESS(2, "进行中"),
    
    /**
     * 待确认
     * <p>接单者已提交成果，等待发布者确认验收</p>
     */
    SUBMIT(3, "待确认"),
    
    // ==================== 结算阶段 ====================
    
    /**
     * 待收款
     * <p>发布者已确认验收，等待系统结算付款</p>
     */
    PAYMENT(4, "待收款"),
    
    // ==================== 争议阶段 ====================
    
    /**
     * 争议中
     * <p>订单存在争议，等待平台介入处理</p>
     */
    DISPUTE(5, "争议中"),
    
    // ==================== 终态 ====================
    
    /**
     * 已完成
     * <p>订单已完成，任务结束（终态）</p>
     */
    COMPLETED(6, "已完成"),
    
    /**
     * 已放弃
     * <p>接单者主动放弃了任务（终态）</p>
     */
    ABANDONED(7, "已放弃"),
    
    // ==================== 评价阶段 ====================
    
    /**
     * 待评价
     * <p>任务已完成，等待双方评价</p>
     */
    RATE(8, "待评价");

    /** 状态码 */
    private final Integer code;
    
    /** 状态描述 */
    private final String desc;

    /**
     * 枚举构造函数
     */
    TradeOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 状态转换规则映射
     * <p>定义每个状态允许转换到的目标状态集合</p>
     */
    private static final Map<TradeOrderStatusEnum, Set<TradeOrderStatusEnum>> TRANSITIONS = new EnumMap<>(TradeOrderStatusEnum.class);

    static {
        // 待审批 → 进行中（同意）、已拒绝（拒绝）
        TRANSITIONS.put(PENDING, EnumSet.of(PROGRESS, REJECTED));

        // 已拒绝 → 终态，无转换
        TRANSITIONS.put(REJECTED, EnumSet.noneOf(TradeOrderStatusEnum.class));

        // 进行中 → 待确认（提交成果）、已放弃（放弃任务）
        TRANSITIONS.put(PROGRESS, EnumSet.of(SUBMIT, ABANDONED));

        // 待确认 → 待收款（确认验收）、争议中（发起申诉）、进行中（打回修改）
        TRANSITIONS.put(SUBMIT, EnumSet.of(PAYMENT, DISPUTE, PROGRESS));

        // 待收款 → 已完成（收款完成）、待评价（收款并进入评价）
        TRANSITIONS.put(PAYMENT, EnumSet.of(COMPLETED, RATE));

        // 争议中 → 已完成（申诉处理完成）、待收款（申诉通过）、进行中（申诉驳回继续执行）
        TRANSITIONS.put(DISPUTE, EnumSet.of(COMPLETED, PAYMENT, PROGRESS));

        // 已完成 → 终态，无转换
        TRANSITIONS.put(COMPLETED, EnumSet.noneOf(TradeOrderStatusEnum.class));

        // 已放弃 → 终态，无转换
        TRANSITIONS.put(ABANDONED, EnumSet.noneOf(TradeOrderStatusEnum.class));

        // 待评价 → 已完成（评价完成）
        TRANSITIONS.put(RATE, EnumSet.of(COMPLETED));
    }

    /**
     * 判断是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return 如果允许转换返回true
     */
    public boolean canTransitionTo(TradeOrderStatusEnum target) {
        if (target == null) {
            return false;
        }
        Set<TradeOrderStatusEnum> allowedTransitions = TRANSITIONS.get(this);
        return allowedTransitions != null && allowedTransitions.contains(target);
    }

    /**
     * 获取允许的转换状态列表
     *
     * @return 允许转换到的状态集合
     */
    public Set<TradeOrderStatusEnum> getAllowedTransitions() {
        return TRANSITIONS.getOrDefault(this, EnumSet.noneOf(TradeOrderStatusEnum.class));
    }

    /**
     * 判断是否为终态
     *
     * @return 如果是终态返回true
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == REJECTED || this == ABANDONED;
    }

    /**
     * 判断是否为活跃状态（可进行操作）
     *
     * @return 如果是活跃状态返回true
     */
    public boolean isActiveStatus() {
        return this == PENDING || this == PROGRESS || this == SUBMIT ||
                this == PAYMENT || this == DISPUTE || this == RATE;
    }

    /**
     * 判断是否可提交成果
     *
     * @return 如果可以提交成果返回true
     */
    public boolean canSubmit() {
        return this == PROGRESS;
    }

    /**
     * 判断是否可确认验收
     *
     * @return 如果可以确认验收返回true
     */
    public boolean canConfirm() {
        return this == SUBMIT;
    }

    /**
     * 判断是否可发起申诉
     *
     * @return 如果可以发起申诉返回true
     */
    public boolean canDispute() {
        return this == SUBMIT;
    }

    /**
     * 判断是否可放弃任务
     *
     * @return 如果可以放弃任务返回true
     */
    public boolean canAbandon() {
        return this == PROGRESS;
    }

    /**
     * 判断是否可审批（同意/拒绝）
     *
     * @return 如果可以审批返回true
     */
    public boolean canApprove() {
        return this == PENDING;
    }

    /**
     * 判断是否可打回修改
     *
     * @return 如果可以打回修改返回true
     */
    public boolean canReject() {
        return this == SUBMIT;
    }

    /**
     * 判断是否需要发布者操作
     *
     * @return 如果需要发布者操作返回true
     */
    public boolean needsPublisherAction() {
        return this == PENDING || this == SUBMIT;
    }

    /**
     * 判断是否需要接单者操作
     *
     * @return 如果需要接单者操作返回true
     */
    public boolean needsAcceptorAction() {
        return this == PROGRESS || this == PAYMENT || this == RATE;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    public static TradeOrderStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeOrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return this == ABANDONED || this == REJECTED;
    }
}
