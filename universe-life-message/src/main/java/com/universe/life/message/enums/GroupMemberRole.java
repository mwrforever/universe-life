package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群组成员角色枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum GroupMemberRole {

    /**
     * 普通成员
     */
    MEMBER(0, "普通成员"),

    /**
     * 管理员
     */
    ADMIN(1, "管理员"),

    /**
     * 群主
     */
    OWNER(2, "群主");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GroupMemberRole of(Integer code) {
        if (code == null) {
            return null;
        }
        for (GroupMemberRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isOwner() {
        return this == OWNER;
    }

    /**
     * 是否有管理权限（管理员或群主）
     */
    public boolean hasAdminPermission() {
        return this == ADMIN || this == OWNER;
    }
}
