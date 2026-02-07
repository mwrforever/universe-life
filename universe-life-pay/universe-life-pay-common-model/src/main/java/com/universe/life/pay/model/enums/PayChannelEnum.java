package com.universe.life.pay.model.enums;

import lombok.Getter;

@Getter
public enum PayChannelEnum {

    WECHAT(0, "微信"),
    ALIPAY(1, "支付宝"),
    BANK_CARD(2, "银行卡");

    private final int code;
    private final String desc;

    PayChannelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayChannelEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayChannelEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
