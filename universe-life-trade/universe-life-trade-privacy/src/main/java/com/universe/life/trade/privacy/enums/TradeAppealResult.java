package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉处理结果枚举
 * <p>
 * 用于数据库持久化的申诉处理结果枚举，与MyBatis-Plus集成。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeAppealResult {
    
    /**
     * 支持申诉方
     * <p>平台判定申诉方胜诉</p>
     */
    SUPPORT(1, "支持申诉方"),
    
    /**
     * 驳回
     * <p>平台驳回申诉请求</p>
     */
    REJECT(2, "驳回");

    /**
     * 结果码
     * <p>用于数据库存储</p>
     */
    @EnumValue
    private final Integer code;
    
    /**
     * 结果描述
     */
    private final String desc;

    /**
     * 获取结果码（用于JSON序列化）
     *
     * @return 结果码
     */
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    /**
     * 根据结果码获取枚举实例
     *
     * @param code 结果码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    @JsonCreator
    public static TradeAppealResult of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeAppealResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        return null;
    }
    
    /**
     * 判断是否支持申诉方
     *
     * @return 如果支持申诉方返回true
     */
    public boolean isSupport() {
        return this == SUPPORT;
    }
}
