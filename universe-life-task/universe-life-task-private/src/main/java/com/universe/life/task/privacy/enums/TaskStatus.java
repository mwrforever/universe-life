package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {
    PENDING(0, "待审核"),
    WAIT_PAY(1, "待支付"),
    PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REJECTED(5, "审核拒绝"),
    OFFLINE(6, "已下架");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TaskStatus of(Integer code) {
        if (code == null) return null;
        for (TaskStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    public static TaskStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
