package com.universe.life.aftercare.model.enums;

import lombok.Getter;

@Getter
public enum AftercareBizType {

    ORDER(1),
    TASK(2);

    private final int code;

    AftercareBizType(int code) {
        this.code = code;
    }

    public static AftercareBizType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AftercareBizType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
