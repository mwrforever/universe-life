package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务审核状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskReviewStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskReviewStatus of(String name) {
        if (name == null) return null;
        for (TaskReviewStatus e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskReviewStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskReviewStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
