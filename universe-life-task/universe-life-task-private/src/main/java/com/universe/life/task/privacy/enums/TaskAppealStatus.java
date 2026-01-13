package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申诉状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskAppealStatus {
    PENDING(0, "待处理"),
    HANDLED(1, "已处理");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TaskAppealStatus of(Integer code) {
        if (code == null) return null;
        for (TaskAppealStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    public static TaskAppealStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskAppealStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
