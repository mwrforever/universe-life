package com.universe.life.model.enums;

/**
 * @author 毛伟然
 * @since 2025/11/4 11:01
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(0, "正常"),
    CAN_RECEIVE(1, "可接单"),
    CAN_PUBLISH(2, "可发单"),
    DISABLE(3, "禁用"),
    ;

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isNormal() {
        return this.equals(NORMAL);
    }

    public boolean isCanReceive() {
        return this.equals(CAN_RECEIVE);
    }

    public boolean isDisabled() {
        return this.equals(DISABLE);
    }

    public boolean isCanPublish() {
        return this.equals(CAN_PUBLISH);
    }

    /**
     * 判断是否为活跃状态（正常、可接单、可发单）
     */
    public boolean isActive() {
        return !isDisabled();
    }

    public boolean canPublish() {
        return isCanPublish();
    }

    /**
     * 判断是否为可接单状态（可接单、可发单）
     */
    public boolean canReceive() {
        return isCanReceive() || isCanPublish();
    }

}
