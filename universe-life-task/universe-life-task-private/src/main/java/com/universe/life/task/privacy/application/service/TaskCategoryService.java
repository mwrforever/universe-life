package com.universe.life.task.privacy.application.service;

import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.privacy.application.assembler.TaskCategoryAssembler;
import com.universe.life.task.privacy.interfaces.dto.response.TaskCategoryDTO;
import com.universe.life.task.privacy.domain.exception.TaskBusinessException;
import com.universe.life.task.privacy.domain.exception.TaskErrorCode;
import com.universe.life.task.privacy.domain.model.valueobject.TaskCategory;
import com.universe.life.task.privacy.domain.repository.TaskCategoryRepository;
import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;
import com.universe.life.task.privacy.infrastructure.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务分类服务
 * 负责处理任务分类管理
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCategoryService {

    private final TaskCategoryRepository taskCategoryRepository;
    private final TaskCategoryAssembler taskCategoryAssembler;
    private final CacheUtil cacheUtil;

    /**
     * 查询分类列表
     * 只返回启用状态的分类（用户端）
     * 
     * @return 分类列表
     */
    public List<TaskCategoryDTO> listCategories() {
        return listCategories(CommonStatus.ENABLE);
    }

    /**
     * 查询分类列表（管理端）
     * 可以查询所有状态的分类
     * 缓存策略：只缓存启用状态的分类，24小时 + 10-30分钟随机过期时间
     * 
     * @param status 状态（null表示查询所有）
     * @return 分类列表
     */
    public List<TaskCategoryDTO> listCategories(CommonStatus status) {
        log.debug("查询分类列表: status={}", status);

        // 只缓存启用状态的分类
        if (status == CommonStatus.ENABLE) {
            String cacheKey = RedisKeyConstants.TASK_CATEGORIES;
            
            // 使用缓存（24小时 + 10-30分钟随机）
            return cacheUtil.getListOrComputeWithRandomExpire(
                    cacheKey,
                    TaskCategoryDTO.class,
                    () -> queryCategoriesFromDb(status),
                    24 * 60
            );
        }
        // 其他状态直接查询数据库
        return queryCategoriesFromDb(status);
    }

    /**
     * 从数据库查询分类列表
     */
    private List<TaskCategoryDTO> queryCategoriesFromDb(CommonStatus status) {
        List<TaskCategory> categories = taskCategoryRepository.findAll(status);
        return categories.stream()
                .map(taskCategoryAssembler::toTaskCategoryDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建分类
     * 
     * @param name 分类名称
     * @param code 分类代码
     * @param sort 排序
     * @return 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(String name, String code, Integer sort) {
        log.info("创建分类: name={}, code={}, sort={}", name, code, sort);

        // 验证名称唯一性
        if (taskCategoryRepository.existsByNameExcludingId(name, null)) {
            throw new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NAME_EXISTS);
        }

        // 验证代码唯一性
        if (taskCategoryRepository.existsByCodeExcludingId(code, null)) {
            throw new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_CODE_EXISTS);
        }

        // 创建分类
        TaskCategory category = TaskCategory.create(name, code, sort);
        TaskCategory savedCategory = taskCategoryRepository.save(category);

        log.info("分类创建成功: categoryId={}", savedCategory.getCategoryId());

        // 删除分类列表缓存
        invalidateCategoryCache();

        return savedCategory.getCategoryId();
    }

    /**
     * 更新分类
     * 
     * @param categoryId 分类ID
     * @param name 分类名称
     * @param sort 排序
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long categoryId, String name, Integer sort) {
        log.info("更新分类: categoryId={}, name={}, sort={}", categoryId, name, sort);

        // 查询分类
        TaskCategory category = taskCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NOT_FOUND));

        // 验证名称唯一性（排除当前分类）
        if (taskCategoryRepository.existsByNameExcludingId(name, categoryId)) {
            throw new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NAME_EXISTS);
        }

        // 更新分类
        category.update(name, sort);
        taskCategoryRepository.save(category);

        log.info("分类更新成功: categoryId={}", categoryId);

        // 删除分类列表缓存
        invalidateCategoryCache();
    }

    /**
     * 禁用分类
     * 
     * @param categoryId 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableCategory(Long categoryId) {
        log.info("禁用分类: categoryId={}", categoryId);

        // 查询分类
        TaskCategory category = taskCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NOT_FOUND));

        // 禁用分类
        category.disable();
        taskCategoryRepository.save(category);

        log.info("分类禁用成功: categoryId={}", categoryId);

        // 删除分类列表缓存
        invalidateCategoryCache();
    }

    /**
     * 启用分类
     * 
     * @param categoryId 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableCategory(Long categoryId) {
        log.info("启用分类: categoryId={}", categoryId);

        // 查询分类
        TaskCategory category = taskCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TaskBusinessException(TaskErrorCode.TASK_CATEGORY_NOT_FOUND));

        // 启用分类
        category.enable();
        taskCategoryRepository.save(category);

        log.info("分类启用成功: categoryId={}", categoryId);

        // 删除分类列表缓存
        invalidateCategoryCache();
    }

    // ==================== 私有方法 ====================

    /**
     * 删除分类列表缓存
     */
    private void invalidateCategoryCache() {
        String cacheKey = RedisKeyConstants.TASK_CATEGORIES;
        cacheUtil.delete(cacheKey);
        log.debug("删除分类列表缓存");
    }
}
