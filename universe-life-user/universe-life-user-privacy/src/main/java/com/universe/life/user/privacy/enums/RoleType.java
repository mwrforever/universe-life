package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型枚举
 *
 * @author Claude
 * @since 2025/11/25
 */
@Getter
@AllArgsConstructor
public enum RoleType {

    /**
     * 系统角色 - 不可修改标识
     */
    SYSTEM(0, "系统角色"),

    /**
     * 业务角色 - 普通业务角色
     */
    BUSINESS(1, "业务角色"),

    /**
     * 自定义角色 - 用户自定义角色
     */
    CUSTOM(2, "自定义角色");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoleType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleType roleType : RoleType.values()) {
            if (roleType.code.equals(code)) {
                return roleType;
            }
        }
        return null;
    }

    public boolean isSystem() {
        return this.equals(SYSTEM);
    }

    public boolean isBusiness() {
        return this.equals(BUSINESS);
    }

    public boolean isCustom() {
        return this.equals(CUSTOM);
    }
}