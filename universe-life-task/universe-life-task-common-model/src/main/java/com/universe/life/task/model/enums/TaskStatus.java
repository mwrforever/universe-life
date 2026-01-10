package com.universe.life.task.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务/需求状态枚举（公共模型）
 * <p>
 * 用于服务间通信，与Task服务内部的TaskStatus保持一致
 * </p>
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {
    AUDIT(0, "待审核"),
    RECRUITING(1, "招募中"),
    PENDING(2, "待审批"),
    PROGRESS(3, "进行中"),
    REVIEW(4, "待验收"),
    PAYMENT(5, "待支付"),
    DISPUTE(6, "争议中"),
    COMPLETED(7, "已完成"),
    REJECTED(8, "已拒绝"),
    OFFLINE(9, "已下架"),
    RATE(10, "待评价");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskStatus of(String name) {
        if (name == null) return null;
        for (TaskStatus e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * 是否可以接单
     */
    public boolean canAccept() {
        return this == RECRUITING;
    }

    /**
     * 是否为终态
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == REJECTED || this == OFFLINE || this == RATE;
    }
}
