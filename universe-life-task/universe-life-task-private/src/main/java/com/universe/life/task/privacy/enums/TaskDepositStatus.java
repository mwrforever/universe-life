package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 保证金状态枚举
 * <p>
 * 定义任务保证金的支付状态，用于跟踪保证金的生命周期。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TaskDepositStatus {
    
    /** 待支付 - 保证金尚未支付 */
    UNPAID(0, "待支付"),
    
    /** 已支付 - 保证金已成功支付，资金已冻结 */
    PAID(1, "已支付"),
    
    /** 已退还 - 保证金已退还给发布者（任务取消等情况） */
    REFUNDED(2, "已退还"),
    
    /** 已结算 - 保证金已结算给接单者（任务完成） */
    SETTLED(3, "已结算");

    /** 状态码，用于数据库存储 */
    @EnumValue
    private final Integer code;
    
    /** 状态描述，用于前端展示 */
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
     * @return 对应的枚举实例，不存在返回null
     */
    @JsonCreator
    public static TaskDepositStatus of(Integer code) {
        if (code == null) return null;
        for (TaskDepositStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，不存在返回null
     */
    public static TaskDepositStatus ofCode(Integer code) {
        return of(code);
    }
}
