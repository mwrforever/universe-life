package com.universe.life.pay.model.enums;

import lombok.Getter;

@Getter
public enum PayStatusEnum {

    CREATED(0, "创建"),
    PAYING(1, "支付中"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败"),
    CLOSED(4, "关闭"),
    REFUND(5, "退款");

    private final int code;
    private final String desc;

    PayStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
