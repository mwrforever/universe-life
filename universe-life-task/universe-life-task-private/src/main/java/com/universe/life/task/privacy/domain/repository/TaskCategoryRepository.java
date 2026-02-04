package com.universe.life.task.privacy.domain.repository;

import com.universe.life.task.privacy.domain.model.TaskCategory;
import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;

import java.util.List;
import java.util.Optional;

/**
 * TaskCategory 仓储接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public interface TaskCategoryRepository {

    /**
     * 保存分类
     */
    TaskCategory save(TaskCategory category);

    /**
     * 根据ID查询分类
     */
    Optional<TaskCategory> findById(Long categoryId);

    /**
     * 查询所有分类（按排序）
     */
    List<TaskCategory> findAll(CommonStatus status);

    /**
     * 根据名称查询分类
     */
    Optional<TaskCategory> findByName(String name);

    /**
     * 根据代码查询分类
     */
    Optional<TaskCategory> findByCode(String code);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    boolean existsByNameExcludingId(String name, Long excludeId);

    /**
     * 检查代码是否存在（排除指定ID）
     */
    boolean existsByCodeExcludingId(String code, Long excludeId);
}
