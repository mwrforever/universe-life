package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉状态枚举
 * <p>
 * 用于数据库持久化的申诉状态枚举，与MyBatis-Plus集成。
 * </p>
 * 
 * <p>申诉状态流转：PENDING → PROCESSED</p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeAppealStatus {
    
    /**
     * 待处理
     * <p>申诉已提交，等待平台处理</p>
     */
    PENDING(0, "待处理"),
    
    /**
     * 已处理
     * <p>申诉已被平台处理完成</p>
     */
    PROCESSED(1, "已处理");

    /**
     * 状态码
     * <p>用于数据库存储</p>
     */
    @EnumValue
    private final Integer code;
    
    /**
     * 状态描述
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
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    @JsonCreator
    public static TradeAppealStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeAppealStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为待处理状态
     *
     * @return 如果是待处理状态返回true
     */
    public boolean isPending() {
        return this == PENDING;
    }
}
