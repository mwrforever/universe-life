package com.universe.life.task.privacy.infrastructure.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务审核状态枚举
 * <p>
 * 定义任务的审核状态，用于管理员审核任务。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TaskReviewStatus {

    /** 待审核 - 任务等待管理员审核 */
    PENDING(0, "待审核"),

    /** 通过 - 任务审核通过，可以在任务大厅展示 */
    APPROVED(1, "通过"),

    /** 拒绝 - 任务审核未通过，需要修改后重新提交 */
    REJECTED(2, "拒绝");

    /** 状态码，用于数据库存储 */
    @EnumValue
    private final Integer code;

    /** 状态描述，用于前端展示 */
    private final String desc;

    /**
     * 获取状态码（用于JSON序列化）
     *
     * @return 状态码
     */
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    /**
     * 根据状态码获取枚举实例（用于JSON反序列化）
     *
     * @param code 状态码
     * @return 对应的枚举实例，不存在返回null
     */
    @JsonCreator
    public static TaskReviewStatus of(Integer code) {
        if (code == null) return null;
        for (TaskReviewStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * 获取状态描述文本
     */
    public String getText() {
        return this.desc;
    }
}
