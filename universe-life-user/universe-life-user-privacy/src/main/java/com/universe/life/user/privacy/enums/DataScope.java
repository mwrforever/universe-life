package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举
 *
 * @author Claude
 * @since 2025/11/25
 */
@Getter
@AllArgsConstructor
public enum DataScope {

    /**
     * 全部数据权限
     */
    ALL(0, "全部"),

    /**
     * 本部门数据权限
     */
    DEPT(1, "本部门"),

    /**
     * 本部门及下级部门数据权限
     */
    DEPT_AND_CHILD(2, "本部门及下级"),

    /**
     * 仅本人数据权限
     */
    SELF(3, "仅自己");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DataScope of(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataScope dataScope : DataScope.values()) {
            if (dataScope.code.equals(code)) {
                return dataScope;
            }
        }
        return null;
    }

    public boolean isAll() {
        return this.equals(ALL);
    }
}