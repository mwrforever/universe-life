package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友请求状态枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum FriendRequestStatus {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已接受
     */
    ACCEPTED(1, "已接受"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已过期
     */
    EXPIRED(3, "已过期");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FriendRequestStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (FriendRequestStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isAccepted() {
        return this == ACCEPTED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }
}
