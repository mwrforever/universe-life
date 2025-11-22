package com.universe.life.auth.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 毛伟然
 * @since 2025/11/11 14:48
 */
@AllArgsConstructor
@Getter
public enum JwkState {
    ACTIVE("ACTIVE"),
    RESOLVED("RESOLVED"),
    INACTIVE("INACTIVE");

    private final String state;

    public JwkState of(String status) {
        if (status == null) {
            return null;
        }
        for (JwkState value : values()) {
            if (value.state.equals(status)) {
                return value;
            }
        }
        return null;
    }

}
