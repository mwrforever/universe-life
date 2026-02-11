package com.universe.life.task.privacy.domain.model.valueobject;

import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaskCategory 实体 - 任务分类领域模型
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCategory {

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String name;

    /** 分类代码 */
    private String code;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private CommonStatus status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    // ==================== 业务方法 ====================

    /**
     * 创建分类
     */
    public static TaskCategory create(String name, String code, Integer sort) {
        return TaskCategory.builder()
                .name(name)
                .code(code)
                .sort(sort)
                .status(CommonStatus.ENABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 更新分类信息
     */
    public void update(String name, Integer sort) {
        this.name = name;
        this.sort = sort;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用分类
     */
    public void disable() {
        this.status = CommonStatus.DISABLE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 启用分类
     */
    public void enable() {
        this.status = CommonStatus.ENABLE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 判断分类是否启用
     */
    public boolean isEnabled() {
        return CommonStatus.ENABLE.equals(this.status);
    }
}
