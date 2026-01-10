package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉类型枚举
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TradeAppealType {
    ACCEPTOR(1, "接单方申诉"),
    PUBLISHER(2, "发布方申诉");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TradeAppealType of(Integer code) {
        if (code == null) return null;
        for (TradeAppealType e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
