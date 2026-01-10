package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友关系状态枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum FriendStatus {

    /**
     * 已删除
     */
    DELETED(0, "已删除"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FriendStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (FriendStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}
