package com.universe.life.pay.model.enums;

import lombok.Getter;

@Getter
public enum BalanceFreezeStatusEnum {

    FROZEN(0, "冻结中"),
    CONFIRMED_DEBIT(1, "已确认扣款"),
    UNFROZEN(2, "已解冻"),
    CLOSED(3, "关闭/失效");

    private final int code;
    private final String desc;

    BalanceFreezeStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BalanceFreezeStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (BalanceFreezeStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
