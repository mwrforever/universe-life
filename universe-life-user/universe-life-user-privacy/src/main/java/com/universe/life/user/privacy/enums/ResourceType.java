package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源类型枚举
 *
 * @author Claude
 * @since 2025/11/25
 */
@Getter
@AllArgsConstructor
public enum ResourceType {

    /**
     * 菜单
     */
    MENU(0, "菜单"),

    /**
     * 按钮
     */
    BUTTON(1, "按钮"),

    /**
     * 接口
     */
    API(2, "接口"),

    /**
     * 数据权限
     */
    DATA(3, "数据权限");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ResourceType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType.code.equals(code)) {
                return resourceType;
            }
        }
        return null;
    }

    public boolean isMenu() {
        return this.equals(MENU);
    }

    public boolean isButton() {
        return this.equals(BUTTON);
    }

    public boolean isApi() {
        return this.equals(API);
    }

    public boolean isData() {
        return this.equals(DATA);
    }
}