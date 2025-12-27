package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申诉结果枚举
 */
@Getter
@AllArgsConstructor
public enum TaskAppealResult {
    SUPPORT(1, "支持申诉方"),
    REJECT(2, "驳回");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskAppealResult of(String name) {
        if (name == null) return null;
        for (TaskAppealResult e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskAppealResult ofCode(Integer code) {
        if (code == null) return null;
        for (TaskAppealResult e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
