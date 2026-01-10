package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉状态枚举
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TradeAppealStatus {
    PENDING(0, "待处理"),
    PROCESSED(1, "已处理");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TradeAppealStatus of(Integer code) {
        if (code == null) return null;
        for (TradeAppealStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
