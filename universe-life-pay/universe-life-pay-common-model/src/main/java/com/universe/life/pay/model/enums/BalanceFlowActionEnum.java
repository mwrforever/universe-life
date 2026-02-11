package com.universe.life.pay.model.enums;

import lombok.Getter;

@Getter
public enum BalanceFlowActionEnum {

    FREEZE("FREEZE", "冻结"),
    UNFREEZE("UNFREEZE", "解冻"),
    CONFIRM_DEBIT("CONFIRM_DEBIT", "确认扣款"),
    CREDIT("CREDIT", "入账"),
    DEBIT("DEBIT", "出账"),
    TRANSFER_OUT("TRANSFER_OUT", "转出"),
    TRANSFER_IN("TRANSFER_IN", "转入");

    private final String code;
    private final String desc;

    BalanceFlowActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BalanceFlowActionEnum of(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (BalanceFlowActionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
