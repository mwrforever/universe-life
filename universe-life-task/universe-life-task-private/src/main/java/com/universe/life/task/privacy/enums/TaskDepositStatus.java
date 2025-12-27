package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 保证金状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskDepositStatus {
    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    REFUNDED(2, "已退还"),
    SETTLED(3, "已结算");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskDepositStatus of(String name) {
        if (name == null) return null;
        for (TaskDepositStatus e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskDepositStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskDepositStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
