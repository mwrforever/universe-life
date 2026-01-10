package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉处理结果枚举
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TradeAppealResult {
    SUPPORT(1, "支持申诉方"),
    REJECT(2, "驳回");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TradeAppealResult of(Integer code) {
        if (code == null) return null;
        for (TradeAppealResult e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }


}
