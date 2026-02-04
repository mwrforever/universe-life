package com.universe.life.task.privacy.infrastructure.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务接受记录状态枚举
 * <p>
 * 定义接单记录的状态，跟踪接单者完成任务的进度。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TaskAcceptanceStatus {

    /** 待同意 - 接单申请等待发布者同意 */
    PENDING_APPROVAL(-1, "待同意"),

    /** 进行中 - 发布者已同意，接单者正在执行任务 */
    PROGRESS(0, "进行中"),

    /** 待确认 - 接单者已提交成果，等待发布者确认 */
    WAIT_CONFIRM(1, "待确认"),

    /** 已完成 - 发布者确认成果，任务完成 */
    COMPLETED(2, "已完成"),

    /** 已放弃 - 接单者主动放弃任务 */
    ABANDONED(3, "已放弃"),

    /** 申诉中 - 接单者对结果有异议，正在申诉 */
    DISPUTE(4, "申诉中"),

    /** 已拒绝 - 发布者拒绝接单申请 */
    REJECTED(5, "已拒绝");

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
    public static TaskAcceptanceStatus of(Integer code) {
        if (code == null) return null;
        for (TaskAcceptanceStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，不存在返回null
     */
    public static TaskAcceptanceStatus ofCode(Integer code) {
        return of(code);
    }
}
