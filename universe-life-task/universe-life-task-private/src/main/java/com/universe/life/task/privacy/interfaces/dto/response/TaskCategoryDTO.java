package com.universe.life.task.privacy.interfaces.dto.response;

import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaskCategory DTO
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCategoryDTO {

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
}
